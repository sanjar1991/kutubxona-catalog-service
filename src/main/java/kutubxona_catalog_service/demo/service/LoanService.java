package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.config.LoanProperties;
import kutubxona_catalog_service.demo.entity.*;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.BookRepository;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanProperties properties;

    /** Hali qaytarilmagan (a'zo qo'lidagi) ijara holatlari. */
    static final Set<LoanStatus> OPEN_STATUSES = EnumSet.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository,
                       LoanProperties properties) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.properties = properties;
    }

    @Transactional
    public Loan borrowBook(Long bookId, Long memberId) {
        // Qulf bilan o'qiladi — parallel so'rovlar navbatma-navbat ishlaydi
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitob", bookId));

        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("A'zo", memberId));

        if (loanRepository.existsByMember_IdAndStatus(memberId, LoanStatus.OVERDUE)) {
            throw new BusinessRuleException("A'zoda muddati o'tgan kitob bor. Avval uni qaytarish kerak");
        }

        if (loanRepository.existsByMember_IdAndBook_IdAndStatusIn(memberId, bookId, OPEN_STATUSES)) {
            throw new BusinessRuleException("Bu a'zo ushbu kitobni allaqachon olgan va hali qaytarmagan");
        }

        if (loanRepository.countByMember_IdAndStatusIn(memberId, OPEN_STATUSES) >= properties.maxActivePerMember()) {
            throw new BusinessRuleException("A'zoda bir vaqtda " + properties.maxActivePerMember()
                    + " tadan ortiq kitob bo'lishi mumkin emas");
        }

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("Kitobdan nusxa qolmagan: " + book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowedDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(properties.periodDays()));
        loan.setStatus(LoanStatus.ACTIVE);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Ijara", loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("Bu kitob allaqachon qaytarilgan");
        }

        loan.setReturnedDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId()).orElseThrow();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    /** dueDate < today bo'lgan ACTIVE ijaralarni OVERDUE qiladi. */
    @Transactional
    public int markOverdueLoans(LocalDate today) {
        return loanRepository.markOverdue(today, LoanStatus.ACTIVE, LoanStatus.OVERDUE);
    }

    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
}
