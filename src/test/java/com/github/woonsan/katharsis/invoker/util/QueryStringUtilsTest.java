package com.github.woonsan.katharsis.invoker.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.github.woonsan.katharsis.invoker.KatharsisInvokerContext;
import com.github.woonsan.katharsis.servlet.ServletKatharsisInvokerContext;

public class QueryStringUtilsTest {

    private static final String QUERY_STRING = "foo=bar&lux=bar&foo=foo";
    private static final String [] FOO_PARAM_VALUES = {"bar", "foo"};
    private static final String [] LUX_PARAM_VALUES = {"bar"};

    KatharsisInvokerContext invokerContext;

    @Before
    public void before() throws Exception {
        ServletContext servletContext = new MockServletContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString(QUERY_STRING);
        request.setParameter("foo", FOO_PARAM_VALUES);
        request.setParameter("lux", LUX_PARAM_VALUES);

        MockHttpServletResponse response = new MockHttpServletResponse();

        invokerContext = new ServletKatharsisInvokerContext(servletContext, request, response);
    }

    @Test
    public void testParseQueryStringAsMultiValuesMap() throws Exception {
        Map<String, String[] > parsedQueryStringMap =  QueryStringUtils.parseQueryStringAsMultiValuesMap(invokerContext);
        assertTrue("parsedQueryStringMap must contain foo.", parsedQueryStringMap.containsKey("foo"));
        assertTrue("parsedQueryStringMap must have 2 values for foo.", parsedQueryStringMap.get("foo").length == 2);
        assertEquals(FOO_PARAM_VALUES[0], parsedQueryStringMap.get("foo")[0]);
        assertEquals(FOO_PARAM_VALUES[1], parsedQueryStringMap.get("foo")[1]);
        assertTrue("parsedQueryStringMap must contain lux.", parsedQueryStringMap.containsKey("lux"));
        assertTrue("parsedQueryStringMap must have 1 value for lux.", parsedQueryStringMap.get("lux").length == 1);
        assertEquals(LUX_PARAM_VALUES[0], parsedQueryStringMap.get("lux")[0]);
    }

    @Test
    public void testParseQueryStringAsSingleValueMap() throws Exception {
        Map<String, String> parsedQueryStringMap =  QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        assertTrue("parsedQueryStringMap must contain foo.", parsedQueryStringMap.containsKey("foo"));
        assertEquals(FOO_PARAM_VALUES[0], parsedQueryStringMap.get("foo"));
        assertTrue("parsedQueryStringMap must contain lux.", parsedQueryStringMap.containsKey("lux"));
        assertEquals(LUX_PARAM_VALUES[0], parsedQueryStringMap.get("lux"));
    }

}
