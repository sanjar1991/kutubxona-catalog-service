package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.BookRequest;
import kutubxona_catalog_service.demo.dto.BookResponse;
import kutubxona_catalog_service.demo.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    /** Sahifalangan ro'yxat: ?page=0&size=20&sort=id,asc */
    @GetMapping
    public PagedModel<BookResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(bookService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
