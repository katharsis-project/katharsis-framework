package io.katharsis.spring.boot;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class SpringBootSimpleExampleApplicationTests {

    @Value("${local.server.port}")
    private int port;

    @Test
    public void testTestEndpointWithQueryParams() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("http://localhost:" + this.port + "/api/tasks?filter[tasks][name]=John", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThatJson(response.getBody()).node("data[0].attributes.name").isStringEqualTo("John");
        assertThatJson(response.getBody()).node("data[0].links.self").isStringEqualTo("http://localhost:8080/api/tasks/1");
        assertThatJson(response.getBody()).node("meta.name").isStringEqualTo("meta information");
    }

    @Test
    public void testTestCustomEndpoint() throws Exception {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("http://localhost:" + this.port + "/api/custom", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), "hello");
    }
}
