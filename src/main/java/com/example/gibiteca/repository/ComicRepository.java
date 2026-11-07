package com.example.gibiteca.repository;

import com.example.gibiteca.model.Comic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Acesso ao banco para a tabela "comic".
 * Só de estender JpaRepository já ganhamos, SEM escrever SQL:
 * save(), findById(), findAll(), count(), deleteById()...
 */
public interface ComicRepository extends JpaRepository<Comic, Long> {

    /**
     * Busca do Hub: procura o texto no título OU no autor e, se um gênero
     * for informado, filtra por ele. Texto vazio ('') = "não filtrar".
     */
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
