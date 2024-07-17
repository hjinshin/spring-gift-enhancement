package gift.controller.restcontroller;

import gift.controller.dto.request.CreateCategoryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryRestControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void port() {
        assertThat(port).isNotZero();
    }

    @Test
    void createCategory() {
        // given
        var url = "http://localhost:" + port + "/api/v1/category";
        var request = new CreateCategoryRequest("category1", "#1", "image1", "");
        var requestEntity = new RequestEntity<>(request, HttpMethod.POST, URI.create(url));

        // when
        var actual = restTemplate.exchange(requestEntity, String.class);

        // then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getBody()).isNotNull();
    }
}