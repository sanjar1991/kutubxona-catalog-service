package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // ---- Soft delete: faqat o'chirilmagan yozuvlar ----
    Page<Author> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Author> findByIdAndDeletedAtIsNull(Long id);

    List<Author> findAllByIdInAndDeletedAtIsNull(Collection<Long> ids);
}