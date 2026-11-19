package kutubxona_catalog_service.demo.stage5;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 5-bosqich (11-band): yagona xato ushlagich.
 * - "topilmadi"            -> 404
 * - biznes qoidasi buzildi -> 409
 * - noto'g'ri so'rov       -> 400
 * Javob tanasi har doim bir xil JSON formatda (RFC 9457 ProblemDetail).
 */
class ErrorHandlingTest extends IntegrationTestBase {

    @Test
    void borrow_unknownBook_returns404WithMessage() throws Exception {
        Member member = saveMember("Ali");

        postJson("/api/loans/borrow", "{\"bookId\":999999,\"memberId\":" + member.getId() + "}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Kitob topilmadi: id=999999"));
    }

    @Test
    void borrow_unknownMember_returns404() throws Exception {
        Book book = saveBook("Sarob", 1);

        postJson("/api/loans/borrow", "{\"bookId\":" + book.getId() + ",\"memberId\":999999}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("A'zo topilmadi: id=999999"));
    }

    @Test
    void borrow_noCopiesLeft_returns409() throws Exception {
        Book book = saveBook("Ufq", 0);
        Member member = saveMember("Vali");

        postJson("/api/loans/borrow", "{\"bookId\":" + book.getId() + ",\"memberId\":" + member.getId() + "}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString("nusxa qolmagan")));
    }

    @Test
    void returnTwice_secondTimeReturns409() throws Exception {
        Long loanId = borrow(saveBook("Navoiy", 1).getId(), saveMember("Hasan").getId());

        mockMvc.perform(post("/api/loans/{id}/return", loanId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/loans/{id}/return", loanId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Bu kitob allaqachon qaytarilgan"));
    }

    @Test
    void returnUnknownLoan_returns404() throws Exception {
        mockMvc.perform(post("/api/loans/{id}/return", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ijara topilmadi: id=999999"));
    }

    @Test
    void getUnknownEntity_returns404WithJsonBodyForEveryResource() throws Exception {
        String[][] cases = {
                {"/api/books/999999", "Kitob topilmadi: id=999999"},
                {"/api/authors/999999", "Muallif topilmadi: id=999999"},
                {"/api/categories/999999", "Kategoriya topilmadi: id=999999"},
                {"/api/members/999999", "A'zo topilmadi: id=999999"},
        };
        for (String[] c : cases) {
            mockMvc.perform(get(c[0]))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value(c[1]));
        }
    }

    @Test
    void malformedJson_returns400() throws Exception {
        postJson("/api/authors", "{ bu json emas")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
