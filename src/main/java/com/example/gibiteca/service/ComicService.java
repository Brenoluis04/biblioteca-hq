package com.example.gibiteca.service;

import com.example.gibiteca.model.Comic;
import com.example.gibiteca.repository.ComicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Regras do CATÁLOGO. O controller (etapa 3) nunca fala direto com o banco:
 * ele chama o service, e o service chama o repository.
 */
@Service
public class ComicService {

    private final ComicRepository comicRepository;

    // Injeção de dependência: o Spring entrega o repository pronto.
    public ComicService(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }

    /** LÊ do banco: HQs do Hub, com busca e filtro de gênero opcionais. */
    public List<Comic> listar(String busca, String genero) {
        return comicRepository.filtrar(limpar(busca), limpar(genero));
    }

    /** LÊ do banco: os gêneros disponíveis para o filtro. */
    public List<String> listarGeneros() {
        return comicRepository.listarGeneros();
    }

    /** LÊ do banco: uma HQ pelo id. */
    public Optional<Comic> buscarPorId(Long id) {
        return comicRepository.findById(id);
    }

    /** ESCREVE no banco: cadastra uma nova HQ no catálogo. */
    public Comic cadastrar(Comic comic) {
        return comicRepository.save(comic);
    }

    /** Null vira texto vazio e espaços das pontas são removidos. */
    private String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
