package com.example.gibiteca.config;

import com.example.gibiteca.model.Comic;
import com.example.gibiteca.repository.ComicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ComicRepository comicRepository;

    public DataSeeder(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }

    @Override
    public void run(String... args) {
        if (comicRepository.count() > 0) {
            return; 
        }

        comicRepository.saveAll(List.of(
            new Comic("Capitão Aurora", "Marina Teles", "Super-herói", null,
                "Uma piloto ganha o poder de controlar a luz do amanhecer e protege Porto Alvorada.", 48),
            new Comic("Os Guardiões do Abismo", "Rafael Duarte", "Fantasia", null,
                "Cinco aventureiros descem às profundezas para impedir o despertar de um antigo dragão.", 120),
            new Comic("Neon Ronin", "Akira Sato", "Cyberpunk", null,
                "Em uma metrópole iluminada por neon, um samurai sem mestre caça dados roubados.", 96),
            new Comic("A Cidade dos Ventos", "Helena Prado", "Aventura", null,
                "Uma jovem cartógrafa descobre uma cidade que muda de lugar a cada tempestade.", 80),
            new Comic("Detetive Noturno", "Bruno Carvalho", "Mistério", null,
                "Um investigador que só trabalha de madrugada resolve crimes que ninguém lembra de manhã.", 64),
            new Comic("Crônicas de Vale Sombrio", "Lívia Montenegro", "Terror", null,
                "Moradores de uma vila isolada percebem que a névoa esconde algo que os observa.", 72),
            new Comic("Pixel Heróis", "Diego Nogueira", "Humor", null,
                "Personagens de videogame retrô fogem do cartucho e tentam sobreviver no mundo real.", 40),
            new Comic("O Último Faroleiro", "Sofia Albuquerque", "Ficção científica", null,
                "Em uma estação espacial esquecida, o último guardião mantém acesa a luz que guia as naves.", 88)
        ));
    }
}
