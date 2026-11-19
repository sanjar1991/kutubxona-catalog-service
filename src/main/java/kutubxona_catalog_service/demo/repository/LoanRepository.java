package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Loan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** book va member bitta so'rovda yuklanadi (N+1 va LAZY proxy muammosiz). */
    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findAll();
}
