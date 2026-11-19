package kutubxona_catalog_service.demo.stage13;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 13-bosqich (13-band): API to'liqligi.
 * - A'zo: PUT va DELETE
 * - Ijara: GET /{id}, memberId va status bo'yicha filtr
 * - Barcha ro'yxatlar sahifalangan: {content: [...], page: {size, number, totalElements, totalPages}}
 */
class ApiCompletenessTest extends IntegrationTestBase {

    // ---------- A'zo: update / delete ----------

    @Test
    void updateMember_changesFields() throws Exception {
        Member member = saveMember("Eski ism");

        putJson("/api/members/" + member.getId(),
                "{\"fullName\":\"Yangi ism\",\"email\":\"yangi@example.com\",\"phone\":\"+998901234567\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Yangi ism"))
                .andExpect(jsonPath("$.email").value("yangi@example.com"))
                .andExpect(jsonPath("$.phone").value("+998901234567"));
    }

    @Test
    void updateMember_invalidOrUnknown() throws Exception {
        Member member = saveMember("Ali");
        putJson("/api/members/" + member.getId(), "{\"fullName\":\"\",\"email\":\"x\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        putJson("/api/members/999999", "{\"fullName\":\"Ali\"}")
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMember_withoutOpenLoans_isSoftDeleted() throws Exception {
        Member member = saveMember("Ali");

        mockMvc.perform(delete("/api/members/{id}", member.getId())).andExpect(status().isNoContent());

        assertThat(memberRepository.findById(member.getId()).orElseThrow().getDeletedAt()).isNotNull();
        mockMvc.perform(get("/api/members/{id}", member.getId())).andExpect(status().isNotFound());
        postJson("/api/loans/borrow",
                "{\"bookId\":" + saveBook("K", 1).getId() + ",\"memberId\":" + member.getId() + "}")
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMember_withOpenLoan_returns409() throws Exception {
        Member member = saveMember("Ali");
        borrow(saveBook("K", 1).getId(), member.getId());

        mockMvc.perform(delete("/api/members/{id}", member.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("A'zoni o'chirib bo'lmaydi: unda qaytarilmagan kitoblar bor"));
    }

    // ---------- Ijara: GET by id va filtrlar ----------

    @Test
    void getLoanById() throws Exception {
        Book book = saveBook("Kitob", 1);
        Member member = saveMember("Ali");
        Long loanId = borrow(book.getId(), member.getId());

        mockMvc.perform(get("/api/loans/{id}", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loanId))
                .andExpect(jsonPath("$.bookTitle").value("Kitob"))
                .andExpect(jsonPath("$.memberFullName").value("Ali"));
        mockMvc.perform(get("/api/loans/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ijara topilmadi: id=999999"));
    }

    @Test
    void filterLoans_byMemberAndStatus() throws Exception {
        Member ali = saveMember("Ali");
        Member vali = saveMember("Vali");
        Long returned = borrow(saveBook("A", 1).getId(), ali.getId());
        mockMvc.perform(post("/api/loans/{id}/return", returned)).andExpect(status().isOk());
        borrow(saveBook("B", 1).getId(), ali.getId());
        borrow(saveBook("C", 1).getId(), vali.getId());

        mockMvc.perform(get("/api/loans").param("memberId", ali.getId().toString()))
                .andExpect(jsonPath("$.page.totalElements").value(2));
        mockMvc.perform(get("/api/loans").param("status", "ACTIVE"))
                .andExpect(jsonPath("$.page.totalElements").value(2));
        mockMvc.perform(get("/api/loans")
                        .param("memberId", ali.getId().toString()).param("status", "ACTIVE"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].bookTitle").value("B"));
        mockMvc.perform(get("/api/loans"))
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void filterLoans_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/loans").param("status", "YOQ"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Pagination ----------

    @Test
    void books_arePaginatedWithDefaultSize20() throws Exception {
        for (int i = 1; i <= 25; i++) saveBook("Kitob " + i, 1);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        mockMvc.perform(get("/api/books").param("page", "1"))
                .andExpect(jsonPath("$.content.length()").value(5));

        mockMvc.perform(get("/api/books").param("size", "5"))
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(5));
    }

    @Test
    void pageSize_isCappedAt100() throws Exception {
        saveBook("Kitob", 1);
        mockMvc.perform(get("/api/books").param("size", "100000"))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void sorting_works() throws Exception {
        saveBook("Alpomish", 1);
        saveBook("Zarbulmasal", 1);
        saveBook("Kecha va kunduz", 1);

        mockMvc.perform(get("/api/books").param("sort", "title,desc"))
                .andExpect(jsonPath("$.content[0].title").value("Zarbulmasal"))
                .andExpect(jsonPath("$.content[2].title").value("Alpomish"));
    }

    @Test
    void sortingByUnknownField_returns400NotServerError() throws Exception {
        mockMvc.perform(get("/api/books").param("sort", "yoqMaydon"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void allListEndpoints_arePaginated() throws Exception {
        for (String url : new String[]{"/api/books", "/api/authors", "/api/categories", "/api/members", "/api/loans"}) {
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page.totalElements").exists());
        }
    }
}
