package kutubxona_catalog_service.demo.stage3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 3-bosqich: springdoc Spring Boot 4 bilan ishlashini tekshiradi.
 * OpenAPI hujjati generatsiya bo'lishi va barcha controller'lar unda ko'rinishi kerak.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired MockMvc mockMvc;

    @Test
    void apiDocs_areGeneratedForAllControllers() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/books']").exists())
                .andExpect(jsonPath("$.paths['/api/authors']").exists())
                .andExpect(jsonPath("$.paths['/api/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/members']").exists())
                .andExpect(jsonPath("$.paths['/api/loans/borrow']").exists());
    }

    @Test
    void swaggerUi_isAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
