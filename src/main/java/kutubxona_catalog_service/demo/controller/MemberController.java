package kutubxona_catalog_service.demo.controller;

import jakarta.validation.Valid;
import kutubxona_catalog_service.demo.dto.MemberRequest;
import kutubxona_catalog_service.demo.dto.MemberResponse;
import kutubxona_catalog_service.demo.service.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    /** Sahifalangan ro'yxat: ?page=0&size=20&sort=id,asc */
    @GetMapping
    public PagedModel<MemberResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(memberService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
