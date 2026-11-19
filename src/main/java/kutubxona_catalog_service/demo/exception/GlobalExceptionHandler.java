package kutubxona_catalog_service.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Barcha controller'lar uchun yagona xato ushlagich.
 * Javoblar RFC 9457 (ProblemDetail) formatida: {type, title, status, detail, instance}.
 * ResponseEntityExceptionHandler Spring'ning standart xatolarini (noto'g'ri JSON,
 * noto'g'ri HTTP metod va h.k.) ham shu formatga keltiradi.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Topilmadi", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.CONFLICT, "Qoida buzildi", ex.getMessage());
    }

    /**
     * Zaxira: bazadagi cheklov buzildi (takroriy unique qiymat, FK va h.k.).
     * Mijozga SQL matni ko'rsatilmaydi — u faqat log'ga yoziladi.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Ma'lumotlar yaxlitligi buzildi: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Ziddiyat",
                "Ma'lumot mavjud yozuvlar bilan ziddiyatda (takroriy qiymat yoki bog'liqlik)");
    }

    /** ?sort=... da mavjud bo'lmagan maydon ko'rsatilgan. */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleBadSort(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Noto'g'ri saralash",
                "Saralash uchun noma'lum maydon: " + ex.getPropertyName());
    }

    /** @Valid xatolari: {"errors": {"title": "Kitob nomi bo'sh bo'lmasligi kerak", ...}} */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));

        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Noto'g'ri ma'lumot",
                "So'rovdagi maydonlar validatsiyadan o'tmadi");
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
