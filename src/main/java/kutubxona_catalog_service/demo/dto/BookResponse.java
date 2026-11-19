package kutubxona_catalog_service.demo.dto;

import kutubxona_catalog_service.demo.entity.Book;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        Integer publishedYear,
        Integer totalCopies,
        Integer availableCopies,
        BigDecimal price,
        List<NamedRef> authors,
        List<NamedRef> categories
) {
    public static BookResponse from(Book b) {
        return new BookResponse(
                b.getId(), b.getTitle(), b.getIsbn(), b.getPublishedYear(),
                b.getTotalCopies(), b.getAvailableCopies(), b.getPrice(),
                b.getAuthors().stream()
                        .map(a -> new NamedRef(a.getId(), a.getFullName()))
                        .sorted(Comparator.comparing(NamedRef::id)).toList(),
                b.getCategories().stream()
                        .map(c -> new NamedRef(c.getId(), c.getName()))
                        .sorted(Comparator.comparing(NamedRef::id)).toList()
        );
    }
}
