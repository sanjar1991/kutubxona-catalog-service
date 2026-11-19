package kutubxona_catalog_service.demo.dto;

import kutubxona_catalog_service.demo.entity.Member;

public record MemberResponse(Long id, String fullName, String email, String phone) {

    public static MemberResponse from(Member m) {
        return new MemberResponse(m.getId(), m.getFullName(), m.getEmail(), m.getPhone());
    }
}
