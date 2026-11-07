package com.example.gibiteca.repository;

import com.example.gibiteca.model.LibraryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Acesso ao banco para a tabela "library_entry" (Minha Biblioteca). */
public interface LibraryEntryRepository extends JpaRepository<LibraryEntry, Long> {

    /** O Spring monta o SQL só pelo nome do método: busca pela HQ do catálogo. */
    Optional<LibraryEntry> findByComicId(Long comicId);

    /** As mais recentes primeiro. */
    List<LibraryEntry> findAllByOrderByAddedAtDescIdDesc();

    /** Ids das HQs já salvas (o Hub usa isso para mostrar "Salvar" ou "Salva"). */
    @Query("SELECT e.comic.id FROM LibraryEntry e")
    List<Long> findAllComicIds();
}
