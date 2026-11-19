package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ---- Soft delete: faqat o'chirilmagan yozuvlar ----
    List<Category> findAllByDeletedAtIsNull();

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    List<Category> findAllByIdInAndDeletedAtIsNull(Collection<Long> ids);
}