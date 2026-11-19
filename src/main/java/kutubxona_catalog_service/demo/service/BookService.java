package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.dto.BookRequest;
import kutubxona_catalog_service.demo.dto.BookResponse;
import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.BaseEntity;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.AuthorRepository;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.CategoryRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       CategoryRepository categoryRepository,
                       LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAllByDeletedAtIsNull(pageable).map(BookResponse::from);
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookResponse.from(getBook(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = new Book();
        apply(book, request);
        book.setAvailableCopies(request.totalCopies());   // yangi kitob: hamma nusxa javonda
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        // Qulf bilan o'qiymiz: shu payt kimdir kitob olsa ham hisob buzilmaydi
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitob", id));

        int borrowed = Math.max(0, nz(book.getTotalCopies()) - nz(book.getAvailableCopies()));
        if (request.totalCopies() < borrowed) {
            throw new BusinessRuleException("Hozir " + borrowed + " ta nusxa a'zolar qo'lida. "
                    + "Jami nusxalar sonini " + borrowed + " tadan kam qilib bo'lmaydi");
        }

        apply(book, request);
        book.setAvailableCopies(request.totalCopies() - borrowed);
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitob", id));
        if (loanRepository.existsByBook_IdAndStatusIn(id, LoanService.OPEN_STATUSES)) {
            throw new BusinessRuleException(
                    "Kitobni o'chirib bo'lmaydi: uning nusxasi hozir a'zolar qo'lida");
        }
        book.markDeleted();   // qaytarilgan ijaralar tarixi saqlanib qoladi
    }

    private Book getBook(Long id) {
        return bookRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitob", id));
    }

    private void apply(Book book, BookRequest request) {
        book.setTitle(request.title().trim());
        book.setIsbn(request.isbn());
        book.setPublishedYear(request.publishedYear());
        book.setTotalCopies(request.totalCopies());
        book.setPrice(request.price());
        book.setAuthors(findAllOrThrow(
                authorRepository::findAllByIdInAndDeletedAtIsNull, request.authorIds(), "Muallif"));
        book.setCategories(findAllOrThrow(
                categoryRepository::findAllByIdInAndDeletedAtIsNull, request.categoryIds(), "Kategoriya"));
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    /** Barcha id'lar bo'yicha (o'chirilmagan) yozuvlarni topadi; birortasi yo'q bo'lsa — 404. */
    private static <T extends BaseEntity> Set<T> findAllOrThrow(
            Function<Set<Long>, List<T>> finder, Set<Long> ids, String resourceName) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<T> found = finder.apply(ids);
        Set<Long> foundIds = found.stream().map(BaseEntity::getId).collect(Collectors.toSet());
        for (Long id : ids) {
            if (!foundIds.contains(id)) {
                throw new ResourceNotFoundException(resourceName, id);
            }
        }
        return new HashSet<>(found);
    }
}
