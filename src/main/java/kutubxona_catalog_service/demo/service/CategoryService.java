package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.dto.CategoryRequest;
import kutubxona_catalog_service.demo.dto.CategoryResponse;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.CategoryRepository;
import kutubxona_catalog_service.demo.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByDeletedAtIsNull().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name().trim());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        category.setName(request.name().trim());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategory(id);
        long books = bookRepository.countByCategories_IdAndDeletedAtIsNull(id);
        if (books > 0) {
            throw new BusinessRuleException(
                    "Kategoriyani o'chirib bo'lmaydi: u " + books + " ta kitobga bog'langan");
        }
        category.markDeleted();
    }

    private Category getCategory(Long id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategoriya", id));
    }
}
