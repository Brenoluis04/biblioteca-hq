# Gibiteca - Biblioteca de HQs

Aplicação local em Java 25 + Spring Boot + Thymeleaf + H2 (arquivo).

## Como rodar
1. Instale o JDK 25 e o Maven (ou use o Maven do IntelliJ/Eclipse).
2. Na pasta do projeto: `mvn spring-boot:run`
3. Abra http://localhost:8080

## Modelo do banco
comic (id, title, author, genre, cover_url, description, total_pages, file_path)
library_entry (id, comic_id UNIQUE -> comic.id, current_page, rating, added_at)

comic 1 --- 0..1 library_entry
