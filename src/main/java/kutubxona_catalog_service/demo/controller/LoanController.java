package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.BorrowRequest;
import kutubxona_catalog_service.demo.dto.LoanResponse;
import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // Kitob olish
    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        Loan loan = loanService.borrowBook(request.bookId(), request.memberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanResponse.from(loan));
    }

    // Kitobni qaytarish
    @PostMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Long id) {
        return LoanResponse.from(loanService.returnBook(id));
    }

    @GetMapping
    public List<LoanResponse> getAllLoans() {
        return loanService.getAllLoans().stream().map(LoanResponse::from).toList();
    }
}
