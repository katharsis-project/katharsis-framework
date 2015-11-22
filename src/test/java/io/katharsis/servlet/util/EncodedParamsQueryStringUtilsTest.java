package io.katharsis.servlet.util;

import io.katharsis.servlet.ServletKatharsisInvokerContext;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;

public class EncodedParamsQueryStringUtilsTest extends QueryStringUtilsTest {

    private static final String QUERY_STRING = "foo%5Basd%5D=bar&lux=bar&foo%5Basd%5D=foo&nameonly&& &=";

    @Before
    public void before() throws Exception {
        ServletContext servletContext = new MockServletContext();

        request = new MockHttpServletRequest();
        request.setQueryString(QUERY_STRING);
        request.setParameter("foo%5Basd%5D", FOO_PARAM_VALUES);
        request.setParameter("lux", LUX_PARAM_VALUES);

        response = new MockHttpServletResponse();

        invokerContext = new ServletKatharsisInvokerContext(servletContext, request, response, false);
    }
}
