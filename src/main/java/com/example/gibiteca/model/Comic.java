package com.example.gibiteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "comic")
public class Comic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "O autor é obrigatório")
    @Column(nullable = false)
    private String author;

    private String genre;

    /** Endereço (URL) de uma imagem de capa. Pode ficar vazio. */
    private String coverUrl;

    @Column(length = 1000)
    private String description;

    @Min(value = 1, message = "Informe ao menos 1 página")
    private int totalPages;

    /** Nome do arquivo da HQ (PDF) guardado na pasta data/comics, se houver. */
    private String filePath;

    public Comic() { }

    public Comic(String title, String author, String genre, String coverUrl,
                 String description, int totalPages) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.description = description;
        this.totalPages = totalPages;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
