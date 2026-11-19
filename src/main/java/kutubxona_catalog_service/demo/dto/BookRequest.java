package kutubxona_catalog_service.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record BookRequest(
        @NotBlank(message = "Kitob nomi bo'sh bo'lmasligi kerak")
        @Size(max = 255)
        String title,

        @Size(max = 20)
        String isbn,

        @Positive
        Integer publishedYear,

        // availableCopies bu yerda yo'q: uni server o'zi hisoblaydi
        @NotNull(message = "Nusxalar soni ko'rsatilishi kerak")
        @PositiveOrZero(message = "Nusxalar soni manfiy bo'lmasligi kerak")
        Integer totalCopies,

        @PositiveOrZero
        BigDecimal price,

        Set<Long> authorIds,
        Set<Long> categoryIds
) {
}
