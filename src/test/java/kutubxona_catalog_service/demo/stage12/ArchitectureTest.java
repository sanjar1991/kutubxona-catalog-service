package kutubxona_catalog_service.demo.stage12;

import jakarta.persistence.Entity;
import kutubxona_catalog_service.demo.entity.BaseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 12-bosqich (12 va 14-band): arxitektura qoidalari.
 * Bu testlar loyiha tuzilmasini "qulflaydi" — kelajakda kimdir qoidani buzsa, test yiqiladi.
 *
 * 1) Har bir @Entity BaseEntity'dan meros oladi (id, audit, soft delete bir xil).
 * 2) Controller'lar repository'ga to'g'ridan-to'g'ri bog'lanmaydi — faqat service orqali.
 */
class ArchitectureTest {

    private static final String BASE_PACKAGE = "kutubxona_catalog_service.demo";

    /** Paketdagi berilgan annotatsiyali barcha klasslarni topadi (Spring konteksti kerak emas). */
    static List<Class<?>> findAnnotated(Class<? extends java.lang.annotation.Annotation> annotation) throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        List<Class<?>> result = new ArrayList<>();
        for (var bd : scanner.findCandidateComponents(BASE_PACKAGE)) {
            result.add(Class.forName(bd.getBeanClassName()));
        }
        return result;
    }

    @Test
    void allEntitiesExtendBaseEntity() throws Exception {
        List<Class<?>> entities = findAnnotated(Entity.class);

        assertThat(entities).as("entity'lar topilishi kerak").isNotEmpty();
        assertThat(entities)
                .as("Har bir @Entity BaseEntity'dan meros olishi kerak")
                .allMatch(BaseEntity.class::isAssignableFrom);
    }

    @Test
    void controllersDoNotDependOnRepositories() throws Exception {
        List<Class<?>> controllers = findAnnotated(RestController.class);
        assertThat(controllers).isNotEmpty();

        List<String> violations = new ArrayList<>();
        for (Class<?> controller : controllers) {
            for (Constructor<?> ctor : controller.getDeclaredConstructors()) {
                for (Class<?> param : ctor.getParameterTypes()) {
                    if (Repository.class.isAssignableFrom(param)) {
                        violations.add(controller.getSimpleName() + " -> " + param.getSimpleName());
                    }
                }
            }
            for (Field field : controller.getDeclaredFields()) {
                if (Repository.class.isAssignableFrom(field.getType())) {
                    violations.add(controller.getSimpleName() + " -> " + field.getType().getSimpleName());
                }
            }
        }

        assertThat(violations)
                .as("Controller'lar repository o'rniga service ishlatishi kerak")
                .isEmpty();
    }
}
