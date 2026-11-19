package kutubxona_catalog_service.demo.exception;

/** So'rov to'g'ri, lekin kutubxona qoidasiga zid (masalan, nusxa qolmagan) -> HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
