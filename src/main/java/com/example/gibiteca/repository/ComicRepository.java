package com.example.gibiteca.repository;

import com.example.gibiteca.model.Comic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComicRepository extends JpaRepository<Comic, Long> {

    @Query("SELECT c FROM Comic c "
         + "WHERE (:busca = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :busca, '%')) "
         + "                   OR LOWER(c.author) LIKE LOWER(CONCAT('%', :busca, '%'))) "
         + "AND (:genero = '' OR c.genre = :genero) "
         + "ORDER BY c.title")
    List<Comic> filtrar(@Param("busca") String busca, @Param("genero") String genero);

    /** Lista os gêneros existentes (sem repetir) para montar o filtro da tela. */
    @Query("SELECT DISTINCT c.genre FROM Comic c WHERE c.genre IS NOT NULL ORDER BY c.genre")
    List<String> listarGeneros();
}
