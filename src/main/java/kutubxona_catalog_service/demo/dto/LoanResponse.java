package kutubxona_catalog_service.demo.dto;

import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.entity.LoanStatus;

import java.time.LocalDate;

/** API'ga qaytariladigan ijara ma'lumoti (entity o'rniga). */
public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberFullName,
        LocalDate borrowedDate,
        LocalDate dueDate,
        LocalDate returnedDate,
        LoanStatus status
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getMember().getId(),
                loan.getMember().getFullName(),
                loan.getBorrowedDate(),
                loan.getDueDate(),
                loan.getReturnedDate(),
                loan.getStatus()
        );
    }
}
