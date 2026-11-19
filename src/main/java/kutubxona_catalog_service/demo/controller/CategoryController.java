package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.CategoryRequest;
import kutubxona_catalog_service.demo.dto.CategoryResponse;
import kutubxona_catalog_service.demo.service.CategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /** Sahifalangan ro'yxat: ?page=0&size=20&sort=id,asc */
    @GetMapping
    public PagedModel<CategoryResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(categoryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
