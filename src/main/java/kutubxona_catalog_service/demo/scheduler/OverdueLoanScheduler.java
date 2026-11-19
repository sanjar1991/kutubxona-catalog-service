package kutubxona_catalog_service.demo.scheduler;

import kutubxona_catalog_service.demo.service.LoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Har kuni (standart: soat 01:00) muddati o'tgan ijaralarni OVERDUE qiladi.
 * Jadval: library.loan.overdue-cron sozlamasi.
 */
@Component
public class OverdueLoanScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueLoanScheduler.class);

    private final LoanService loanService;

    public OverdueLoanScheduler(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(cron = "${library.loan.overdue-cron:0 0 1 * * *}")
    public void markOverdueLoans() {
        int changed = loanService.markOverdueLoans(LocalDate.now());
        log.info("Muddati o'tgan ijaralar belgilandi: {} ta", changed);
    }
}
