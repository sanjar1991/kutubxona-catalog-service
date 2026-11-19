package kutubxona_catalog_service.demo.stage9;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 9-bosqich (7-band): kitob olish cheklovlari.
 * 1) A'zo bir xil kitobni qaytarmasdan qayta ololmaydi.
 * 2) A'zoda bir vaqtda ko'pi bilan 5 ta kitob bo'lishi mumkin (standart sozlama).
 */
class BorrowLimitsTest extends IntegrationTestBase {

    private static final int MAX_ACTIVE = 5;

    private org.springframework.test.web.servlet.ResultActions tryBorrow(Long bookId, Long memberId) throws Exception {
        return postJson("/api/loans/borrow", "{\"bookId\":" + bookId + ",\"memberId\":" + memberId + "}");
    }

    @Test
    void sameMemberSameBook_secondBorrowRejected() throws Exception {
        Book book = saveBook("Kitob", 3);
        Member member = saveMember("Ali");
        borrow(book.getId(), member.getId());

        tryBorrow(book.getId(), member.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Bu a'zo ushbu kitobni allaqachon olgan va hali qaytarmagan"));

        assertThat(loanRepository.count()).isEqualTo(1);
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void sameMemberSameBook_allowedAgainAfterReturn() throws Exception {
        Book book = saveBook("Kitob", 1);
        Member member = saveMember("Ali");
        Long loanId = borrow(book.getId(), member.getId());
        mockMvc.perform(post("/api/loans/{id}/return", loanId)).andExpect(status().isOk());

        tryBorrow(book.getId(), member.getId()).andExpect(status().isCreated());
    }

    @Test
    void differentMembersSameBook_allowed() throws Exception {
        Book book = saveBook("Kitob", 2);
        borrow(book.getId(), saveMember("Ali").getId());

        tryBorrow(book.getId(), saveMember("Vali").getId()).andExpect(status().isCreated());
    }

    @Test
    void memberCannotExceedActiveLoanLimit() throws Exception {
        Member member = saveMember("Kitobxon");
        List<Long> loanIds = new ArrayList<>();
        for (int i = 0; i < MAX_ACTIVE; i++) {
            loanIds.add(borrow(saveBook("Kitob " + i, 1).getId(), member.getId()));
        }
        Book extra = saveBook("Ortiqcha kitob", 1);

        tryBorrow(extra.getId(), member.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A'zoda bir vaqtda " + MAX_ACTIVE
                        + " tadan ortiq kitob bo'lishi mumkin emas"));
        assertThat(bookRepository.findById(extra.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);

        // Bittasini qaytargach, yana olish mumkin
        mockMvc.perform(post("/api/loans/{id}/return", loanIds.get(0))).andExpect(status().isOk());
        tryBorrow(extra.getId(), member.getId()).andExpect(status().isCreated());
    }
}
