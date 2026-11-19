package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Ijaralarni filtrlab, sahifalab qaytaradi. Filtr null bo'lsa — hisobga olinmaydi.
     * book va member bitta so'rovda yuklanadi (N+1 muammosiz).
     */
    @EntityGraph(attributePaths = {"book", "member"})
    @Query("select l from Loan l where (:memberId is null or l.member.id = :memberId) "
            + "and (:status is null or l.status = :status)")
    Page<Loan> search(@Param("memberId") Long memberId,
                      @Param("status") LoanStatus status,
                      Pageable pageable);

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
