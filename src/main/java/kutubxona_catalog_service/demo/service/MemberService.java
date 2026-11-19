package kutubxona_catalog_service.demo.service;

import kutubxona_catalog_service.demo.dto.MemberRequest;
import kutubxona_catalog_service.demo.dto.MemberResponse;
import kutubxona_catalog_service.demo.entity.Member;
import kutubxona_catalog_service.demo.exception.ResourceNotFoundException;
import kutubxona_catalog_service.demo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAllByDeletedAtIsNull().stream().map(MemberResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return MemberResponse.from(memberRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("A'zo", id)));
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        Member member = new Member();
        member.setFullName(request.fullName().trim());
        member.setEmail(request.email());
        member.setPhone(request.phone());
        return MemberResponse.from(memberRepository.save(member));
    }
}
