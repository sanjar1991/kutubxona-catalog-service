package kutubxona_catalog_service.demo.repository;

import jakarta.persistence.LockModeType;
import kutubxona_catalog_service.demo.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * SELECT ... FOR UPDATE: tranzaksiya tugaguncha boshqa so'rovlar
     * shu kitob qatorini o'zgartira olmaydi va navbat kutadi.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id and b.deletedAt is null")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    /** Muallifga bog'langan (o'chirilmagan) kitoblar soni. */
    long countByAuthors_IdAndDeletedAtIsNull(Long authorId);

    /** Kategoriyaga bog'langan (o'chirilmagan) kitoblar soni. */
    long countByCategories_IdAndDeletedAtIsNull(Long categoryId);

    // ---- Soft delete: faqat o'chirilmagan yozuvlar ----
    Page<Book> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Book> findByIdAndDeletedAtIsNull(Long id);
}
