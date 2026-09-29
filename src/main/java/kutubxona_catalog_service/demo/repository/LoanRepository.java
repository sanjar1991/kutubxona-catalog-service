package kutubxona_catalog_service.demo.repository;

import kutubxona_catalog_service.demo.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
}