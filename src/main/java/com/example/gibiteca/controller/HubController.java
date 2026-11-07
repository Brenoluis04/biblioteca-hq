package com.example.gibiteca.controller;

import com.example.gibiteca.service.ComicService;
import com.example.gibiteca.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Tela principal (Hub): lista o catálogo com busca e filtro por gênero.
 * O controller recebe o pedido do navegador, pede os dados ao service
 * e entrega para a tela (template "hub.html").
 */
@Controller
public class HubController {

    private final ComicService comicService;
    private final LibraryService libraryService;

    public HubController(ComicService comicService, LibraryService libraryService) {
        this.comicService = comicService;
        this.libraryService = libraryService;
    }

    @GetMapping("/")
    public String hub(@RequestParam(name = "busca", defaultValue = "") String busca,
                      @RequestParam(name = "genero", defaultValue = "") String genero,
                      Model model) {
        model.addAttribute("comics", comicService.listar(busca, genero));   // LÊ do banco
        model.addAttribute("generos", comicService.listarGeneros());        // LÊ do banco
        model.addAttribute("idsSalvos", libraryService.idsSalvos());        // LÊ do banco
        model.addAttribute("busca", busca);
        model.addAttribute("genero", genero);
        return "hub";
    }
}
