package kutubxona_catalog_service.demo.exception;

/** So'ralgan yozuv bazada yo'q -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " topilmadi: id=" + id);
    }
}
