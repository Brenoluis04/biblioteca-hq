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

/** Cadastro, edição e exclusão de HQs (com upload opcional de PDF) e abertura do PDF. */
@Controller
public class ComicController {

    private final ComicService comicService;
    private final String pastaHqs;   

    public ComicController(ComicService comicService,
                           @Value("${app.pasta-hqs}") String pastaHqs) {
        this.comicService = comicService;
        this.pastaHqs = pastaHqs;
    }

    @GetMapping("/comics/novo")
    public String formulario(Model model) {
        model.addAttribute("comic", new Comic());
        prepararFormulario(model, "/comics", "Cadastrar HQ", false);
        return "comic-form";
    }

    @PostMapping("/comics")
    public String cadastrar(@Valid @ModelAttribute("comic") Comic comic,
                            BindingResult result,
                            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
                            Model model,
                            RedirectAttributes redirect) {

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
            prepararFormulario(model, "/comics", "Cadastrar HQ", false);
            return "comic-form";
        }

        comicService.cadastrar(comic);                                  
        redirect.addFlashAttribute("sucesso", "HQ \"" + comic.getTitle() + "\" cadastrada!");
        return "redirect:/";
    }

    @GetMapping("/comics/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        Optional<Comic> comic = comicService.buscarPorId(id);           
        if (comic.isEmpty()) {
            redirect.addFlashAttribute("erro", "HQ não encontrada");
            return "redirect:/";
        }
        model.addAttribute("comic", comic.get());
        prepararFormulario(model, "/comics/" + id + "/editar", "Editar HQ",
                comic.get().getFilePath() != null);
        return "comic-form";
    }

    @PostMapping("/comics/{id}/editar")
    public String salvarEdicao(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("comic") Comic comic,
                               BindingResult result,
                               @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
                               Model model,
                               RedirectAttributes redirect) {
        Optional<Comic> atual = comicService.buscarPorId(id);          
        if (atual.isEmpty()) {
            redirect.addFlashAttribute("erro", "HQ não encontrada");
            return "redirect:/";
        }
        String arquivoAntigo = atual.get().getFilePath();
        comic.setId(id);
        comic.setFilePath(null);

        boolean temArquivo = arquivo != null && !arquivo.isEmpty();
        if (temArquivo && !ehPdf(arquivo)) {
            result.rejectValue("filePath", "arquivo.invalido", "O arquivo precisa ser um PDF");
        }

        String novoArquivo = null;
        if (!result.hasErrors() && temArquivo) {
            try {
                novoArquivo = salvarPdf(arquivo);
            } catch (IOException e) {
                result.rejectValue("filePath", "arquivo.erro", "Não foi possível salvar o arquivo");
            }
        }

        if (result.hasErrors()) {
            prepararFormulario(model, "/comics/" + id + "/editar", "Editar HQ", arquivoAntigo != null);
            return "comic-form";
        }

        comicService.atualizar(id, comic, novoArquivo);                 
        if (novoArquivo != null && arquivoAntigo != null) {
            apagarArquivo(arquivoAntigo);                               
        }
        redirect.addFlashAttribute("sucesso", "HQ \"" + comic.getTitle() + "\" atualizada!");
        return "redirect:/";
    }

    @PostMapping("/comics/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirect) {
        String titulo = comicService.buscarPorId(id).map(Comic::getTitle).orElse("HQ");
        Optional<String> arquivo = comicService.excluir(id);            // ESCREVE no banco
        arquivo.ifPresent(this::apagarArquivo);
        redirect.addFlashAttribute("sucesso", "\"" + titulo + "\" foi excluída do catálogo.");
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

    /** Dados que o formulário (cadastro e edição) precisa. */
    private void prepararFormulario(Model model, String acao, String tituloPagina, boolean temPdf) {
        model.addAttribute("acao", acao);
        model.addAttribute("tituloPagina", tituloPagina);
        model.addAttribute("temPdf", temPdf);
        model.addAttribute("generos", comicService.listarGeneros());    
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

    /** Apaga um PDF da pasta de HQs (só dentro dela). Se falhar, o banco já está correto. */
    private void apagarArquivo(String nome) {
        try {
            Path pasta = Paths.get(pastaHqs).toAbsolutePath().normalize();
            Path arquivo = pasta.resolve(nome).normalize();
            if (arquivo.startsWith(pasta)) {
                Files.deleteIfExists(arquivo);
            }
        } catch (IOException e) {
            
        }
    }
}
