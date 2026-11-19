package kutubxona_catalog_service.demo.stage4;

import kutubxona_catalog_service.demo.entity.Book;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import kutubxona_catalog_service.demo.service.LoanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 4-bosqich: bir vaqtning o'zida kelgan so'rovlar oxirgi nusxani
 * bir necha marta bermasligi kerak (race condition / lost update).
 */
@SpringBootTest
class ConcurrentBorrowTest {

    private static final int THREADS = 10;

    @Autowired LoanService loanService;
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
    void concurrentBorrows_neverGiveMoreCopiesThanAvailable() throws Exception {
        // Tayyorlash: 2 nusxali kitob va 10 ta a'zo
        Book book = new Book();
        book.setTitle("Mehrobdan chayon");
        book.setTotalCopies(2);
        book.setAvailableCopies(2);
        Long bookId = bookRepository.save(book).getId();

        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Member m = new Member();
            m.setFullName("A'zo " + i);
            m.setEmail("member" + i + "@example.com");
            memberIds.add(memberRepository.save(m).getId());
        }

        // Harakat: 10 ta oqim bir vaqtda "start" signalini kutib, kitob oladi
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (Long memberId : memberIds) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    loanService.borrowBook(bookId, memberId);
                    success.incrementAndGet();
                } catch (IllegalStateException e) {
                    rejected.incrementAndGet(); // "nusxa qolmagan"
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        // Tekshirish: aynan 2 ta muvaffaqiyat, bazada ham hammasi mos
        assertThat(success.get()).isEqualTo(2);
        assertThat(rejected.get()).isEqualTo(THREADS - 2);
        assertThat(loanRepository.count()).isEqualTo(2);
        assertThat(bookRepository.findById(bookId).orElseThrow().getAvailableCopies()).isZero();
    }
}
