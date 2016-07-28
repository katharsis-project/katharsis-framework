package io.katharsis.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringBootSimpleExampleApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public class SpringBootSimpleExampleApplicationTests {

    @Value("${local.server.port}")
    private int port;

    @Test
    public void testTestEndpointWithQueryParams() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:" + this.port + "/api/tasks?filter[Task][name]=John", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThatJson(response.getBody()).node("data[0].attributes.name").isStringEqualTo("John");
        assertThatJson(response.getBody()).node("data[0].links.self").isStringEqualTo("http://localhost:8080/api/tasks/1");
        assertThatJson(response.getBody()).node("meta.name").isStringEqualTo("meta information");
    }

    @Test
    public void testTestCustomEndpoint() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:" + this.port + "/api/tasks/1", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), "hello");
    }
}
