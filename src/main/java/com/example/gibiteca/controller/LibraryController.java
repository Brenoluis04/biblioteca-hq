package com.example.gibiteca.controller;

import com.example.gibiteca.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Minha Biblioteca: listar, salvar, remover, atualizar progresso e dar nota.
 * Depois de cada ação (POST) redirecionamos para uma página (GET),
 * assim o F5 não repete a ação.
 */
@Controller
@RequestMapping("/biblioteca")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("entradas", libraryService.listar());   // LÊ do banco
        return "biblioteca";
    }

    @PostMapping("/salvar/{comicId}")
    public String salvar(@PathVariable("comicId") Long comicId,
                         @RequestHeader(value = "Referer", required = false) String referer,
                         RedirectAttributes redirect) {
        try {
            libraryService.salvar(comicId);                          // ESCREVE no banco
            redirect.addFlashAttribute("sucesso", "HQ salva na sua biblioteca!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return voltar(referer, "/");
    }

    @PostMapping("/remover/{comicId}")
    public String remover(@PathVariable("comicId") Long comicId,
                          @RequestHeader(value = "Referer", required = false) String referer,
                          RedirectAttributes redirect) {
        libraryService.remover(comicId);                             // ESCREVE no banco
        redirect.addFlashAttribute("sucesso", "HQ removida da sua biblioteca.");
        return voltar(referer, "/biblioteca");
    }

    @PostMapping("/{comicId}/progresso")
    public String progresso(@PathVariable("comicId") Long comicId,
                            @RequestParam("pagina") int pagina,
                            RedirectAttributes redirect) {
        try {
            libraryService.atualizarProgresso(comicId, pagina);      // ESCREVE no banco
            redirect.addFlashAttribute("sucesso", "Progresso atualizado!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/biblioteca";
    }

    @PostMapping("/{comicId}/nota")
    public String nota(@PathVariable("comicId") Long comicId,
                       @RequestParam("nota") int nota,
                       RedirectAttributes redirect) {
        try {
            libraryService.avaliar(comicId, nota);                   // ESCREVE no banco
            redirect.addFlashAttribute("sucesso", "Nota registrada!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/biblioteca";
    }

    /** Volta para a página de onde o usuário veio (mantém busca e filtros do Hub). */
    private String voltar(String referer, String padrao) {
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : padrao);
    }
}
