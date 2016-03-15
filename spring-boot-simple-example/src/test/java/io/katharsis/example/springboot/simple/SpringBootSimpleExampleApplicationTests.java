package io.katharsis.example.springboot.simple;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;

public class SpringBootSimpleExampleApplicationTests extends BaseTest {
    
    @Before
    public void setup() {
    }
    
    @Test
    public void testFindOne() {
    	testFindOne("/api/tasks/1");
    	testFindOne("/api/projects/123");
    }
    
    @Test
    public void testFindOne_NotFound() {
    	testFindOne_NotFound("/api/tasks/0");
    	testFindOne_NotFound("/api/projects/0");
    }
    
    @Test
    public void testFindMany() {
    	testFindMany("/api/tasks");
    	testFindMany("/api/projects");
    }
    
    @Test
    public void testDelete() {
    	testDelete("/api/tasks/1");
    	testDelete("/api/projects/123");
    }
  
    @Test
    public void testCreateTask() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>()
            .put("my-name", "Getter Done")
            .put("description", "12345678901234567890")
            .build();
        
        Map<String, Object> dataMap = ImmutableMap
            .of("data", ImmutableMap
              .of("type", "tasks", "attributes", attributeMap));
        
        ValidatableResponse response = RestAssured.given()
            .contentType("application/json")
            .body(dataMap)
            .when()
            .post("/api/tasks")
            .then()
            .statusCode(CREATED.value());
        response
        	.assertThat()
        	.body(matchesJsonSchema(jsonApiSchema));
    }
    
    @Test
    public void testUpdateTask() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>()
            .put("my-name", "Gotter Did")
            .put("description", "12345678901234567890")
            .build();
        
        Map<String, Object> dataMap = ImmutableMap
            .of("data", ImmutableMap
                .of("type", "tasks", "id", 1, "attributes", attributeMap));
        
        RestAssured.given()
            .contentType("application/json")
            .body(dataMap)
            .when()
            .patch("/api/tasks/1")
            .then()
            .statusCode(OK.value());
    } 
    
    @Test
    public void testUpdateTask_withDescriptionTooLong() {
        Map<String, Object> attributeMap = new ImmutableMap.Builder<String, Object>()
            .put("description", "123456789012345678901")
            .build();
        
        Map<String, Object> dataMap = ImmutableMap
            .of("data", ImmutableMap
                .of("type", "tasks", "id", 1, "attributes", attributeMap));
        
        ValidatableResponse response = RestAssured.given()
            .contentType("application/json")
            .body(dataMap)
            .when()
            .patch("/api/tasks/1")
            .then()
            .statusCode(CONFLICT.value());
        response
        	.assertThat()
        	.body(matchesJsonSchema(jsonApiSchema));
    }
}
