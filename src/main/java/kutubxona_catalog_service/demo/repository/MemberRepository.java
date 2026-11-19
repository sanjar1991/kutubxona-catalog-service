package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // ---- Soft delete: faqat o'chirilmagan yozuvlar ----
    List<Member> findAllByDeletedAtIsNull();

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);
}