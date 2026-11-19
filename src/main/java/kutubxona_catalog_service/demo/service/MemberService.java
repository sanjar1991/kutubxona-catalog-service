package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.dto.MemberRequest;
import kutubxona_catalog_service.demo.dto.MemberResponse;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.exception.BusinessRuleException;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.LoanRepository;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository, LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> findAll(Pageable pageable) {
        return memberRepository.findAllByDeletedAtIsNull(pageable).map(MemberResponse::from);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return MemberResponse.from(getMember(id));
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        Member member = new Member();
        apply(member, request);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMember(id);
        apply(member, request);
        return MemberResponse.from(memberRepository.saveAndFlush(member));
    }

    @Transactional
    public void delete(Long id) {
        Member member = getMember(id);
        if (loanRepository.countByMember_IdAndStatusIn(id, LoanService.OPEN_STATUSES) > 0) {
            throw new BusinessRuleException("A'zoni o'chirib bo'lmaydi: unda qaytarilmagan kitoblar bor");
        }
        member.markDeleted();
    }

    private Member getMember(Long id) {
        return memberRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("A'zo", id));
    }

    private static void apply(Member member, MemberRequest request) {
        member.setFullName(request.fullName().trim());
        member.setEmail(request.email());
        member.setPhone(request.phone());
    }
}
