package com.example.gibiteca.controller;

import com.example.gibiteca.service.ComicService;
import com.example.gibiteca.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        model.addAttribute("comics", comicService.listar(busca, genero));   
        model.addAttribute("generos", comicService.listarGeneros());        
        model.addAttribute("idsSalvos", libraryService.idsSalvos());        
        model.addAttribute("busca", busca);
        model.addAttribute("genero", genero);
        return "hub";
    }
}
