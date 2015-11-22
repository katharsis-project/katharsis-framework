package io.katharsis.servlet.util;

import io.katharsis.servlet.ServletKatharsisInvokerContext;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;

public class DecodedParamsQueryStringUtilsTest extends QueryStringUtilsTest {

    @Before
    public void before() throws Exception {
        ServletContext servletContext = new MockServletContext();

        request = new MockHttpServletRequest();
        request.setQueryString(QUERY_STRING);
        request.setParameter("foo[asd]", FOO_PARAM_VALUES);
        request.setParameter("lux", LUX_PARAM_VALUES);

        response = new MockHttpServletResponse();

        invokerContext = new ServletKatharsisInvokerContext(servletContext, request, response, true);
    }

}
