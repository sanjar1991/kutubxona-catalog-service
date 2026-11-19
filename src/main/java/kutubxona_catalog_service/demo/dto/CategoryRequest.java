package kutubxona_catalog_service.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Kategoriya nomi bo'sh bo'lmasligi kerak")
        @Size(max = 100)
        String name
) {
}
