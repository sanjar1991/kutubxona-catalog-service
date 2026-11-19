package kutubxona_catalog_service.demo.stage10;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Loan;
import kutubxona_catalog_service.demo.entity.LoanStatus;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.scheduler.OverdueLoanScheduler;
import kutubxona_catalog_service.demo.service.LoanService;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 10-bosqich (5-band): muddati o'tgan ijaralar OVERDUE holatiga o'tadi.
 * - faqat ACTIVE va dueDate < bugun bo'lganlar o'zgaradi;
 * - OVERDUE ijarani qaytarish mumkin;
 * - muddati o'tgan kitobi bor a'zoga yangi kitob berilmaydi.
 */
class OverdueLoansTest extends IntegrationTestBase {

    @Autowired LoanService loanService;
    @Autowired OverdueLoanScheduler scheduler;

    private final LocalDate today = LocalDate.now();

    /** Kitob beradi va ijaraning qaytarish muddatini sun'iy ravishda o'zgartiradi. */
    private Long borrowWithDueDate(Book book, Member member, LocalDate dueDate) throws Exception {
        Long loanId = borrow(book.getId(), member.getId());
        Loan loan = loanRepository.findById(loanId).orElseThrow();
        loan.setDueDate(dueDate);
        loanRepository.save(loan);
        return loanId;
    }

    private LoanStatus statusOf(Long loanId) {
        return loanRepository.findById(loanId).orElseThrow().getStatus();
    }

    @Test
    void markOverdue_changesOnlyActiveLoansPastDueDate() throws Exception {
        Long pastDue  = borrowWithDueDate(saveBook("A", 1), saveMember("1"), today.minusDays(1));
        Long dueToday = borrowWithDueDate(saveBook("B", 1), saveMember("2"), today);
        Long returned = borrowWithDueDate(saveBook("C", 1), saveMember("3"), today.minusDays(5));
        mockMvc.perform(post("/api/loans/{id}/return", returned)).andExpect(status().isOk());

        int changed = loanService.markOverdueLoans(today);

        assertThat(changed).isEqualTo(1);
        assertThat(statusOf(pastDue)).isEqualTo(LoanStatus.OVERDUE);
        assertThat(statusOf(dueToday)).isEqualTo(LoanStatus.ACTIVE);     // bugun hali muddat ichida
        assertThat(statusOf(returned)).isEqualTo(LoanStatus.RETURNED);   // qaytarilgan tegilmaydi
    }

    @Test
    void markOverdue_isIdempotent() throws Exception {
        borrowWithDueDate(saveBook("A", 1), saveMember("1"), today.minusDays(1));

        assertThat(loanService.markOverdueLoans(today)).isEqualTo(1);
        assertThat(loanService.markOverdueLoans(today)).isZero();       // ikkinchi marta hech narsa o'zgarmaydi
    }

    @Test
    void scheduler_marksOverdueLoans() throws Exception {
        Long loanId = borrowWithDueDate(saveBook("A", 1), saveMember("1"), today.minusDays(3));

        scheduler.markOverdueLoans();

        assertThat(statusOf(loanId)).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    void overdueLoan_isVisibleInApiAndCanBeReturned() throws Exception {
        Book book = saveBook("A", 1);
        Long loanId = borrowWithDueDate(book, saveMember("1"), today.minusDays(2));
        loanService.markOverdueLoans(today);

        mockMvc.perform(get("/api/loans"))
                .andExpect(jsonPath("$.content[0].status").value("OVERDUE"));

        mockMvc.perform(post("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void memberWithOverdueLoan_cannotBorrowUntilReturned() throws Exception {
        Member member = saveMember("Qarzdor");
        Long overdueLoan = borrowWithDueDate(saveBook("A", 1), member, today.minusDays(1));
        loanService.markOverdueLoans(today);
        Book other = saveBook("B", 1);

        postJson("/api/loans/borrow", "{\"bookId\":" + other.getId() + ",\"memberId\":" + member.getId() + "}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("A'zoda muddati o'tgan kitob bor. Avval uni qaytarish kerak"));

        mockMvc.perform(post("/api/loans/{id}/return", overdueLoan)).andExpect(status().isOk());
        borrow(other.getId(), member.getId());   // endi ruxsat
    }
}
