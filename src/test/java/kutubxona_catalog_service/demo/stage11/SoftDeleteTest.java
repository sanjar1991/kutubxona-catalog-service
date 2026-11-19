package kutubxona_catalog_service.demo.stage11;

import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 11-bosqich (9-band): soft delete.
 * DELETE yozuvni bazadan o'chirmaydi — deletedAt to'ldiriladi va yozuv API'dan yashiriladi.
 * Shu tufayli ijara tarixi saqlanib qoladi.
 */
class SoftDeleteTest extends IntegrationTestBase {

    // ---------- Kitob ----------

    @Test
    void deleteBook_keepsRowButHidesItFromApi() throws Exception {
        Book deleted = saveBook("O'chiriladigan", 1);
        saveBook("Qoladigan", 1);

        mockMvc.perform(delete("/api/books/{id}", deleted.getId())).andExpect(status().isNoContent());

        // Bazada qator bor, lekin belgilangan
        Book row = bookRepository.findById(deleted.getId()).orElseThrow();
        assertThat(row.getDeletedAt()).isNotNull();

        // API'da ko'rinmaydi
        mockMvc.perform(get("/api/books/{id}", deleted.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/books"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Qoladigan"));
    }

    @Test
    void deleteBook_twice_secondReturns404() throws Exception {
        Book book = saveBook("Kitob", 1);
        mockMvc.perform(delete("/api/books/{id}", book.getId())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/books/{id}", book.getId())).andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_withOnlyReturnedLoans_isAllowedAndHistoryIsKept() throws Exception {
        Book book = saveBook("Tarixiy kitob", 1);
        Long loanId = borrow(book.getId(), saveMember("A").getId());
        mockMvc.perform(post("/api/loans/{id}/return", loanId)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/books/{id}", book.getId())).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/loans"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("Tarixiy kitob"))
                .andExpect(jsonPath("$[0].status").value("RETURNED"));
    }

    @Test
    void deleteBook_withOpenLoan_stillReturns409() throws Exception {
        Book book = saveBook("Kitob", 1);
        borrow(book.getId(), saveMember("A").getId());

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Kitobni o'chirib bo'lmaydi: uning nusxasi hozir a'zolar qo'lida"));
        assertThat(bookRepository.findById(book.getId()).orElseThrow().getDeletedAt()).isNull();
    }

    @Test
    void deletedBook_cannotBeBorrowedOrUpdated() throws Exception {
        Book book = saveBook("Kitob", 3);
        mockMvc.perform(delete("/api/books/{id}", book.getId())).andExpect(status().isNoContent());

        postJson("/api/loans/borrow",
                "{\"bookId\":" + book.getId() + ",\"memberId\":" + saveMember("A").getId() + "}")
                .andExpect(status().isNotFound());
        putJson("/api/books/" + book.getId(), "{\"title\":\"Yangi\",\"totalCopies\":3}")
                .andExpect(status().isNotFound());
    }

    // ---------- Muallif va kategoriya ----------

    @Test
    void deleteAuthor_isSoftAndHiddenFromApi() throws Exception {
        Author author = saveAuthor("Hamid Olimjon");

        mockMvc.perform(delete("/api/authors/{id}", author.getId())).andExpect(status().isNoContent());

        assertThat(authorRepository.findById(author.getId()).orElseThrow().getDeletedAt()).isNotNull();
        mockMvc.perform(get("/api/authors/{id}", author.getId())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/authors")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deletedAuthor_cannotBeLinkedToNewBook() throws Exception {
        Author author = saveAuthor("Zulfiya");
        mockMvc.perform(delete("/api/authors/{id}", author.getId())).andExpect(status().isNoContent());

        postJson("/api/books", "{\"title\":\"X\",\"totalCopies\":1,\"authorIds\":[" + author.getId() + "]}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Muallif topilmadi: id=" + author.getId()));
    }

    @Test
    void author_linkedOnlyToDeletedBooks_canBeDeleted() throws Exception {
        Author author = saveAuthor("Muallif");
        Book book = saveBook("Kitob", 1);
        book.getAuthors().add(author);
        bookRepository.save(book);
        mockMvc.perform(delete("/api/books/{id}", book.getId())).andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/authors/{id}", author.getId())).andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_isSoftAndHiddenFromApi() throws Exception {
        Category category = saveCategory("Detektiv");

        mockMvc.perform(delete("/api/categories/{id}", category.getId())).andExpect(status().isNoContent());

        assertThat(categoryRepository.findById(category.getId()).orElseThrow().getDeletedAt()).isNotNull();
        mockMvc.perform(get("/api/categories/{id}", category.getId())).andExpect(status().isNotFound());
    }
}
