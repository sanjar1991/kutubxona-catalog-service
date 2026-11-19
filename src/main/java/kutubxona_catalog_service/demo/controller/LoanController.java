package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.BorrowRequest;
import kutubxona_catalog_service.demo.dto.LoanResponse;
import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.entity.LoanStatus;
import kutubxona_catalog_service.demo.service.LoanService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    /** Filtrlangan va sahifalangan ro'yxat: ?memberId=1&status=ACTIVE&page=0&size=20 */
    @GetMapping
    public PagedModel<LoanResponse> getLoans(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LoanStatus status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(loanService.search(memberId, status, pageable).map(LoanResponse::from));
    }

    @GetMapping("/{id}")
    public LoanResponse getLoan(@PathVariable Long id) {
        return LoanResponse.from(loanService.getLoan(id));
    }
}
