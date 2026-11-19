package kutubxona_catalog_service.demo.support;

import com.jayway.jsonpath.JsonPath;
import kutubxona_catalog_service.demo.entity.Author;
import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Category;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Barcha integratsion testlar uchun umumiy asos:
 * - bitta Spring konteksti (tez ishlaydi, chunki kontekst keshlanadi);
 * - har testdan keyin baza tozalanadi (testlar bir-biriga ta'sir qilmaydi);
 * - test ma'lumotlarini yaratish uchun yordamchi metodlar.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected BookRepository bookRepository;
    @Autowired protected AuthorRepository authorRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected MemberRepository memberRepository;
    @Autowired protected LoanRepository loanRepository;

    @AfterEach
    void cleanDatabase() {
        // Tartib muhim: avval bog'langan (bola) jadvallar o'chiriladi
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    protected Book saveBook(String title, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return bookRepository.save(book);
    }

    protected Member saveMember(String fullName) {
        Member member = new Member();
        member.setFullName(fullName);
        member.setEmail(UUID.randomUUID() + "@example.com");
        return memberRepository.save(member);
    }

    protected Author saveAuthor(String fullName) {
        Author author = new Author();
        author.setFullName(fullName);
        return authorRepository.save(author);
    }

    protected Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    protected ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions putJson(String url, String json) throws Exception {
        return mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Kitob oladi (201 kutiladi) va yaratilgan ijara id'sini qaytaradi. */
    protected Long borrow(Long bookId, Long memberId) throws Exception {
        String body = postJson("/api/loans/borrow",
                "{\"bookId\":" + bookId + ",\"memberId\":" + memberId + "}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
