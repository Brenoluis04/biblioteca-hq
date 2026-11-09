package com.example.gibiteca.service;

import com.example.gibiteca.model.Comic;
import com.example.gibiteca.repository.ComicRepository;
import com.example.gibiteca.repository.LibraryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ComicService {

    private final ComicRepository comicRepository;
    private final LibraryEntryRepository entryRepository;

    // Injeção de dependência: o Spring entrega os repositories prontos.
    public ComicService(ComicRepository comicRepository,
                        LibraryEntryRepository entryRepository) {
        this.comicRepository = comicRepository;
        this.entryRepository = entryRepository;
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

    @Transactional
    public Comic atualizar(Long id, Comic dados, String novoArquivo) {
        Comic comic = comicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("HQ não encontrada"));
        comic.setTitle(dados.getTitle());
        comic.setAuthor(dados.getAuthor());
        comic.setGenre(dados.getGenre());
        comic.setCoverUrl(dados.getCoverUrl());
        comic.setDescription(dados.getDescription());
        comic.setTotalPages(dados.getTotalPages());
        if (novoArquivo != null) {
            comic.setFilePath(novoArquivo);
        }
        return comicRepository.save(comic);
    }

    @Transactional
    public Optional<String> excluir(Long id) {
        Optional<Comic> comic = comicRepository.findById(id);
        if (comic.isEmpty()) {
            return Optional.empty();
        }
        // A entrada da biblioteca aponta para a HQ, então sai primeiro.
        entryRepository.findByComicId(id).ifPresent(entryRepository::delete);
        entryRepository.flush();
        comicRepository.delete(comic.get());
        return Optional.ofNullable(comic.get().getFilePath());
    }

    /** Null vira texto vazio e espaços das pontas são removidos. */
    private String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
