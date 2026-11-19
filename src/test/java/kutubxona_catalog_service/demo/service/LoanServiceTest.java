package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.config.LoanProperties;
import kutubxona_catalog_service.demo.entity.*;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * LoanService biznes qoidalari — unit test (Spring va baza ishlatilmaydi,
 * repository'lar Mockito bilan almashtiriladi).
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock BookRepository bookRepository;
    @Mock MemberRepository memberRepository;
    LoanService loanService;

    Book book;
    Member member;

    @BeforeEach
    void setUp() {
        // maxActivePerMember = 2, periodDays = 14
        loanService = new LoanService(loanRepository, bookRepository, memberRepository,
                new LoanProperties(2, 14));
        book = new Book();
        book.setId(1L);
        book.setTitle("Kecha va kunduz");
        book.setAvailableCopies(1);
        member = new Member();
        member.setId(7L);
        member.setFullName("Ali");
    }

    @Test
    void borrow_success_decrementsCopiesAndCreatesActiveLoan() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(member));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.borrowBook(1L, 7L);

        assertThat(book.getAvailableCopies()).isZero();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getBorrowedDate()).isEqualTo(LocalDate.now());
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
    }

    @Test
    void borrow_unknownBook_throwsNotFound() {
        when(bookRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrowBook(99L, 7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Kitob topilmadi");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_unknownMember_throwsNotFound() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrowBook(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("A'zo topilmadi");
    }

    @Test
    void borrow_noCopiesLeft_throwsBusinessRuleAndDoesNotSave() {
        book.setAvailableCopies(0);
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> loanService.borrowBook(1L, 7L))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(book.getAvailableCopies()).isZero();
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_sameBookAlreadyOpen_throwsBusinessRule() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(member));
        when(loanRepository.existsByMember_IdAndBook_IdAndStatusIn(eq(7L), eq(1L), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> loanService.borrowBook(1L, 7L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("allaqachon olgan");
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrow_limitReached_throwsBusinessRule() {
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(memberRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(member));
        when(loanRepository.countByMember_IdAndStatusIn(eq(7L), anyCollection())).thenReturn(2L);

        assertThatThrownBy(() -> loanService.borrowBook(1L, 7L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 tadan ortiq");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void return_success_incrementsCopiesAndMarksReturned() {
        book.setAvailableCopies(0);
        Loan loan = activeLoan();
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.returnBook(5L);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnedDate()).isEqualTo(LocalDate.now());
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void return_alreadyReturned_throwsBusinessRule() {
        Loan loan = activeLoan();
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("allaqachon qaytarilgan");
        verify(bookRepository, never()).save(any());
    }

    private Loan activeLoan() {
        Loan loan = new Loan();
        loan.setId(5L);
        loan.setBook(book);
        loan.setMember(member);
        loan.setStatus(LoanStatus.ACTIVE);
        return loan;
    }
}
