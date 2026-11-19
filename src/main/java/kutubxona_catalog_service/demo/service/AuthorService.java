package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.dto.AuthorRequest;
import kutubxona_catalog_service.demo.dto.AuthorResponse;
import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.AuthorRepository;
import kutubxona_catalog_service.demo.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuthorResponse> findAll(Pageable pageable) {
        return authorRepository.findAllByDeletedAtIsNull(pageable).map(AuthorResponse::from);
    }

    @Transactional(readOnly = true)
    public AuthorResponse findById(Long id) {
        return AuthorResponse.from(getAuthor(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        Author author = new Author();
        apply(author, request);
        return AuthorResponse.from(authorRepository.save(author));
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);
        apply(author, request);
        return AuthorResponse.from(authorRepository.saveAndFlush(author));
    }

    @Transactional
    public void delete(Long id) {
        Author author = getAuthor(id);
        long books = bookRepository.countByAuthors_IdAndDeletedAtIsNull(id);
        if (books > 0) {
            throw new BusinessRuleException(
                    "Muallifni o'chirib bo'lmaydi: u " + books + " ta kitobga bog'langan");
        }
        author.markDeleted();
    }

    private Author getAuthor(Long id) {
        return authorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Muallif", id));
    }

    private static void apply(Author author, AuthorRequest request) {
        author.setFullName(request.fullName().trim());
        author.setBiography(request.biography());
    }
}
