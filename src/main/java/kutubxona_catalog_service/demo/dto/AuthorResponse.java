package kutubxona_catalog_service.demo.dto;

import kutubxona_catalog_service.demo.entity.Author;

import java.time.LocalDateTime;

public record AuthorResponse(
        Long id,
        String fullName,
        String biography,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AuthorResponse from(Author a) {
        return new AuthorResponse(a.getId(), a.getFullName(), a.getBiography(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
