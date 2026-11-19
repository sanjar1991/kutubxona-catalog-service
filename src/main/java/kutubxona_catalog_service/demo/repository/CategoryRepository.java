package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ---- Soft delete: faqat o'chirilmagan yozuvlar ----
    Page<Category> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    List<Category> findAllByIdInAndDeletedAtIsNull(Collection<Long> ids);
}