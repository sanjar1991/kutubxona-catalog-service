package kutubxona_catalog_service.demo.stage2;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 2-bosqich: /api/loans javoblari DTO orqali qaytishi kerak.
 * Loan.book va Loan.member LAZY — entity'ni to'g'ridan-to'g'ri JSON'ga
 * aylantirish Hibernate proxy sababli xato beradi.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoanResponseTest {

    @Autowired MockMvc mockMvc;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired LoanRepository loanRepository;

    @AfterEach
    void cleanUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void getAllLoans_returnsFlatJsonWithBookAndMemberInfo() throws Exception {
        Book book = new Book();
        book.setTitle("O'tkan kunlar");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        book = bookRepository.save(book);

        Member member = new Member();
        member.setFullName("Ali Valiyev");
        member.setEmail("ali@example.com");
        member = memberRepository.save(member);

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + book.getId() + ",\"memberId\":" + member.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(book.getId()))
                .andExpect(jsonPath("$.memberId").value(member.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Asosiy tekshiruv: findAll() LAZY proxy'lar bilan qaytadi
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(book.getId()))
                .andExpect(jsonPath("$[0].bookTitle").value("O'tkan kunlar"))
                .andExpect(jsonPath("$[0].memberId").value(member.getId()))
                .andExpect(jsonPath("$[0].memberFullName").value("Ali Valiyev"))
                .andExpect(jsonPath("$[0].dueDate").exists());
    }
}
