package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.entity.*;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Loan borrowBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Kitob topilmadi: id=" + bookId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("A'zo topilmadi: id=" + memberId));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Kitobdan nusxa qolmagan: " + book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowedDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Ijara topilmadi: id=" + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Bu kitob allaqachon qaytarilgan");
        }

        loan.setReturnedDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }
}