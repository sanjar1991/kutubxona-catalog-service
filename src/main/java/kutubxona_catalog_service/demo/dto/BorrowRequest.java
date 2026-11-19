package kutubxona_catalog_service.demo.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull(message = "bookId ko'rsatilishi kerak") Long bookId,
        @NotNull(message = "memberId ko'rsatilishi kerak") Long memberId
) {
}
