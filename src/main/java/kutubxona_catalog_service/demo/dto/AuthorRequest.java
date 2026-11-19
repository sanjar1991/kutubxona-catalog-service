package kutubxona_catalog_service.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank(message = "Muallif ismi bo'sh bo'lmasligi kerak")
        @Size(max = 255)
        String fullName,

        @Size(max = 255)
        String biography
) {
}
