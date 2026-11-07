package com.example.gibiteca.controller;

import com.example.gibiteca.model.Comic;
import com.example.gibiteca.service.ComicService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/** Cadastro de HQs (com upload opcional de PDF) e abertura do PDF para leitura. */
@Controller
public class ComicController {

    private final ComicService comicService;
    private final String pastaHqs;   // vem do application.properties (app.pasta-hqs)

    public ComicController(ComicService comicService,
                           @Value("${app.pasta-hqs}") String pastaHqs) {
        this.comicService = comicService;
        this.pastaHqs = pastaHqs;
    }

    @GetMapping("/comics/novo")
    public String formulario(Model model) {
        model.addAttribute("comic", new Comic());
        model.addAttribute("generos", comicService.listarGeneros());   // LÊ do banco
        return "comic-form";
    }

    @PostMapping("/comics")
    public String cadastrar(@Valid @ModelAttribute("comic") Comic comic,
                            BindingResult result,
                            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
                            Model model,
                            RedirectAttributes redirect) {
        // Segurança básica: o formulário não pode definir id nem caminho de arquivo.
        comic.setId(null);
        comic.setFilePath(null);

        boolean temArquivo = arquivo != null && !arquivo.isEmpty();
        if (temArquivo && !ehPdf(arquivo)) {
            result.rejectValue("filePath", "arquivo.invalido", "O arquivo precisa ser um PDF");
        }

        if (!result.hasErrors() && temArquivo) {
            try {
                comic.setFilePath(salvarPdf(arquivo));
            } catch (IOException e) {
                result.rejectValue("filePath", "arquivo.erro", "Não foi possível salvar o arquivo");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("generos", comicService.listarGeneros());
            return "comic-form";
        }

        comicService.cadastrar(comic);                                  // ESCREVE no banco
        redirect.addFlashAttribute("sucesso", "HQ \"" + comic.getTitle() + "\" cadastrada!");
        return "redirect:/";
    }

    /** Entrega o PDF para o navegador abrir (aceita #page=N na URL). */
    @GetMapping("/comics/{id}/arquivo")
    public ResponseEntity<Resource> arquivo(@PathVariable("id") Long id) {
        Optional<Comic> comic = comicService.buscarPorId(id);           // LÊ do banco
        if (comic.isEmpty() || comic.get().getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Path pasta = Paths.get(pastaHqs).toAbsolutePath().normalize();
        Path arquivo = pasta.resolve(comic.get().getFilePath()).normalize();
        if (!arquivo.startsWith(pasta) || !Files.exists(arquivo)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(new FileSystemResource(arquivo));
    }

    private boolean ehPdf(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        return nome != null && nome.toLowerCase().endsWith(".pdf");
    }

    /** Copia o PDF para data/comics com um nome único e devolve esse nome. */
    private String salvarPdf(MultipartFile arquivo) throws IOException {
        Path pasta = Paths.get(pastaHqs).toAbsolutePath().normalize();
        Files.createDirectories(pasta);
        String nome = UUID.randomUUID() + ".pdf";
        arquivo.transferTo(pasta.resolve(nome));
        return nome;
    }
}
