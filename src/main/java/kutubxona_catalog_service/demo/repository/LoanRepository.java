package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.entity.LoanStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** book va member bitta so'rovda yuklanadi (N+1 va LAZY proxy muammosiz). */
    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findAll();

    boolean existsByBook_IdAndStatusIn(Long bookId, Collection<LoanStatus> statuses);

    boolean existsByMember_IdAndBook_IdAndStatusIn(Long memberId, Long bookId, Collection<LoanStatus> statuses);

    long countByMember_IdAndStatusIn(Long memberId, Collection<LoanStatus> statuses);

    boolean existsByMember_IdAndStatus(Long memberId, LoanStatus status);

    /**
     * Bitta UPDATE so'rovi bilan muddati o'tgan barcha ACTIVE ijaralarni OVERDUE qiladi.
     * @return o'zgargan qatorlar soni
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Loan l set l.status = :overdue where l.status = :active and l.dueDate < :today")
    int markOverdue(@Param("today") LocalDate today,
                    @Param("active") LoanStatus active,
                    @Param("overdue") LoanStatus overdue);
}
