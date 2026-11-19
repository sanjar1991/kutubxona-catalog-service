package kutubxona_catalog_service.demo.controller;

import kutubxona_catalog_service.demo.dto.LoanResponse;
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

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // Kitob olish
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody Map<String, Long> request) {
        try {
            LoanResponse loan = LoanResponse.from(
                    loanService.borrowBook(request.get("bookId"), request.get("memberId")));
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Kitobni qaytarish
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(LoanResponse.from(loanService.returnBook(id)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<LoanResponse> getAllLoans() {
        return loanService.getAllLoans().stream().map(LoanResponse::from).toList();
    }
}
