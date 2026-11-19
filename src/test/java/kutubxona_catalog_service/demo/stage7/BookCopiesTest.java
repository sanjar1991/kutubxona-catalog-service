package kutubxona_catalog_service.demo.stage7;

import com.jayway.jsonpath.JsonPath;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 7-bosqich (6-band): availableCopies har doim to'g'ri bo'lishi kerak.
 * Qoida: availableCopies = totalCopies - (hozir qo'lda turgan nusxalar).
 * Mijoz availableCopies'ni to'g'ridan-to'g'ri o'zgartira olmaydi.
 */
class BookCopiesTest extends IntegrationTestBase {

    @Test
    void createBook_availableCopiesEqualsTotal() throws Exception {
        postJson("/api/books", "{\"title\":\"Kitob\",\"totalCopies\":3}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCopies").value(3))
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void createBook_availableCopiesFromClientIsIgnored() throws Exception {
        postJson("/api/books", "{\"title\":\"Kitob\",\"totalCopies\":3,\"availableCopies\":100}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void updateBook_availableCopiesFromClientIsIgnored() throws Exception {
        Book book = saveBook("Kitob", 3);

        putJson("/api/books/" + book.getId(), "{\"title\":\"Kitob\",\"totalCopies\":3,\"availableCopies\":100}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void increaseTotal_keepsBorrowedCount() throws Exception {
        Book book = saveBook("Kitob", 3);
        borrow(book.getId(), saveMember("A").getId());          // 1 ta qo'lda -> available 2

        putJson("/api/books/" + book.getId(), "{\"title\":\"Kitob\",\"totalCopies\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCopies").value(5))
                .andExpect(jsonPath("$.availableCopies").value(4));  // 5 - 1
    }

    @Test
    void decreaseTotal_downToBorrowedCount_isAllowed() throws Exception {
        Book book = saveBook("Kitob", 3);
        borrow(book.getId(), saveMember("A").getId());
        borrow(book.getId(), saveMember("B").getId());           // 2 ta qo'lda

        putJson("/api/books/" + book.getId(), "{\"title\":\"Kitob\",\"totalCopies\":2}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(0));
    }

    @Test
    void decreaseTotal_belowBorrowedCount_returns409AndChangesNothing() throws Exception {
        Book book = saveBook("Kitob", 3);
        borrow(book.getId(), saveMember("A").getId());
        borrow(book.getId(), saveMember("B").getId());           // 2 ta qo'lda

        putJson("/api/books/" + book.getId(), "{\"title\":\"Yangi nom\",\"totalCopies\":1}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("2 ta nusxa")));

        Book unchanged = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Kitob");
        assertThat(unchanged.getTotalCopies()).isEqualTo(3);
        assertThat(unchanged.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowAndReturnCycle_restoresAvailableCopies() throws Exception {
        Book book = saveBook("Kitob", 2);
        Long loanId = borrow(book.getId(), saveMember("A").getId());

        String body = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/books/" + book.getId()))
                .andReturn().getResponse().getContentAsString();
        assertThat((Integer) JsonPath.read(body, "$.availableCopies")).isEqualTo(1);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk());
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(2);
    }
}
