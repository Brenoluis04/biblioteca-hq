package com.example.gibiteca.service;

import com.example.gibiteca.model.Comic;
import com.example.gibiteca.model.LibraryEntry;
import com.example.gibiteca.repository.ComicRepository;
import com.example.gibiteca.repository.LibraryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Regras da MINHA BIBLIOTECA: salvar, remover, progresso e nota.
 * @Transactional = se algo der erro no meio, o banco desfaz a operação inteira.
 */
@Service
public class LibraryService {

    private final LibraryEntryRepository entryRepository;
    private final ComicRepository comicRepository;

    public LibraryService(LibraryEntryRepository entryRepository,
                          ComicRepository comicRepository) {
        this.entryRepository = entryRepository;
        this.comicRepository = comicRepository;
    }

    /** LÊ do banco: todas as HQs salvas. */
    public List<LibraryEntry> listar() {
        return entryRepository.findAllByOrderByAddedAtDescIdDesc();
    }

    /** LÊ do banco: ids das HQs salvas (para o Hub marcar as que já estão na biblioteca). */
    public Set<Long> idsSalvos() {
        return new HashSet<>(entryRepository.findAllComicIds());
    }

    /** LÊ do banco: a entrada de uma HQ específica (se estiver salva). */
    public Optional<LibraryEntry> buscarPorComic(Long comicId) {
        return entryRepository.findByComicId(comicId);
    }

    /** ESCREVE no banco: salva uma HQ na biblioteca (se já estiver salva, não duplica). */
    @Transactional
    public LibraryEntry salvar(Long comicId) {
        Optional<LibraryEntry> existente = entryRepository.findByComicId(comicId);
        if (existente.isPresent()) {
            return existente.get();
        }
        Comic comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new IllegalArgumentException("HQ não encontrada"));
        return entryRepository.save(new LibraryEntry(comic));
    }

    /** ESCREVE no banco: remove uma HQ da biblioteca (ela continua no catálogo). */
    @Transactional
    public void remover(Long comicId) {
        entryRepository.findByComicId(comicId).ifPresent(entryRepository::delete);
    }

    /** ESCREVE no banco: atualiza a página em que o leitor parou (entre 0 e o total). */
    @Transactional
    public LibraryEntry atualizarProgresso(Long comicId, int pagina) {
        LibraryEntry entrada = buscarOuErro(comicId);
        int total = entrada.getComic().getTotalPages();
        entrada.setCurrentPage(Math.max(0, Math.min(pagina, total)));
        return entryRepository.save(entrada);
    }

    /** ESCREVE no banco: dá a nota de 0 a 5 estrelas (só HQs salvas podem ser avaliadas). */
    @Transactional
    public LibraryEntry avaliar(Long comicId, int nota) {
        if (nota < 0 || nota > 5) {
            throw new IllegalArgumentException("A nota deve ser de 0 a 5");
        }
        LibraryEntry entrada = buscarOuErro(comicId);
        entrada.setRating(nota);
        return entryRepository.save(entrada);
    }

    private LibraryEntry buscarOuErro(Long comicId) {
        return entryRepository.findByComicId(comicId)
                .orElseThrow(() -> new IllegalArgumentException("Salve a HQ na biblioteca primeiro"));
    }
}
