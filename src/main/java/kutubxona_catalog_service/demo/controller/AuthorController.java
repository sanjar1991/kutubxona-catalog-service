package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.AuthorRequest;
import kutubxona_catalog_service.demo.dto.AuthorResponse;
import kutubxona_catalog_service.demo.service.AuthorService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    /** Sahifalangan ro'yxat: ?page=0&size=20&sort=id,asc */
    @GetMapping
    public PagedModel<AuthorResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(authorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public AuthorResponse getById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    @PutMapping("/{id}")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
