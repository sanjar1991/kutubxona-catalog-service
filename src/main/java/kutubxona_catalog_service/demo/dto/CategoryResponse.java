package kutubxona_catalog_service.demo.dto;

import kutubxona_catalog_service.demo.entity.Category;

public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
