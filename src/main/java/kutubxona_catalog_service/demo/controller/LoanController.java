package kutubxona_catalog_service.demo.controller;

import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;

    public LoanController(LoanService loanService, LoanRepository loanRepository) {
        this.loanService = loanService;
        this.loanRepository = loanRepository;
    }

    // Kitob olish
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody Map<String, Long> request) {
        try {
            Loan loan = loanService.borrowBook(request.get("bookId"), request.get("memberId"));
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Kitobni qaytarish
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            Loan loan = loanService.returnBook(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
}