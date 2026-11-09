package com.example.gibiteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "library_entry")
public class LibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "comic_id", nullable = false, unique = true)
    private Comic comic;

    /** Página em que o leitor parou (0 = ainda não começou). */
    private int currentPage = 0;

    /** Nota de 0 a 5 estrelas. Null = ainda sem nota. */
    private Integer rating;

    private LocalDate addedAt = LocalDate.now();

    public LibraryEntry() { }

    public LibraryEntry(Comic comic) {
        this.comic = comic;
    }

    /** Porcentagem lida (0 a 100). Calculada na hora, não vai para o banco. */
    @Transient
    public int getProgressPercent() {
        if (comic == null || comic.getTotalPages() <= 0) return 0;
        int pct = (int) Math.round(currentPage * 100.0 / comic.getTotalPages());
        return Math.min(pct, 100);
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comic getComic() { return comic; }
    public void setComic(Comic comic) { this.comic = comic; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public LocalDate getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDate addedAt) { this.addedAt = addedAt; }
}
