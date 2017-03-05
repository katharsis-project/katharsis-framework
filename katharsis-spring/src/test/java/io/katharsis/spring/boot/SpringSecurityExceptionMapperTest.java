package io.katharsis.spring.boot;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.spring.internal.AccessDeniedExceptionMapper;

public class SpringSecurityExceptionMapperTest {

  @Test
  public void testAccessDenied() {
    AccessDeniedExceptionMapper mapper = new AccessDeniedExceptionMapper();
    AccessDeniedException exception = new AccessDeniedException("hi");
    ErrorResponse response = mapper.toErrorResponse(exception);
    Iterable<ErrorData> errors = response.getErrors();
    Iterator<ErrorData> iterator = errors.iterator();
    ErrorData data = iterator.next();
    Assert.assertFalse(iterator.hasNext());
    Assert.assertEquals("403", data.getStatus());
    Assert.assertEquals("hi", data.getCode());
    Assert.assertTrue(mapper.accepts(response));
    AccessDeniedException fromErrorResponse = mapper.fromErrorResponse(response);
    Assert.assertEquals("hi", fromErrorResponse.getMessage());
  }
}
