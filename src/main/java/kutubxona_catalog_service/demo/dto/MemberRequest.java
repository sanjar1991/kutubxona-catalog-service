package kutubxona_catalog_service.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberRequest(
        @NotBlank(message = "A'zo ismi bo'sh bo'lmasligi kerak")
        @Size(max = 255)
        String fullName,

        @Email(message = "Email noto'g'ri formatda")
        @Size(max = 255)
        String email,

        @Size(max = 20)
        String phone
) {
}
