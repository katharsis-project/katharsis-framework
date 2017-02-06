package io.katharsis.servlet.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import io.katharsis.invoker.internal.KatharsisInvokerContext;
import io.katharsis.servlet.internal.QueryStringUtils;
import io.katharsis.servlet.internal.ServletKatharsisInvokerContext;

public class QueryStringUtilsTest {

    private static final String QUERY_STRING = "foo%5Basd%5D=bar&lux=bar&foo%5Basd%5D=foo&nameonly&& &=";
    private static final String[] FOO_PARAM_VALUES = {"bar", "foo"};
    private static final String[] LUX_PARAM_VALUES = {"bar"};

    private KatharsisInvokerContext invokerContext;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void before() throws Exception {
        ServletContext servletContext = new MockServletContext();

        request = new MockHttpServletRequest();
        request.setQueryString(QUERY_STRING);
        request.setParameter("foo[asd]", FOO_PARAM_VALUES);
        request.setParameter("lux", LUX_PARAM_VALUES);

        response = new MockHttpServletResponse();

        invokerContext = new ServletKatharsisInvokerContext(servletContext, request, response);
    }


    @Test
    public void testParseQueryStringAsMultiValuesMap() throws Exception {
        Map<String, String[]> parsedQueryStringMap = QueryStringUtils.parseQueryStringAsMultiValuesMap(invokerContext);
        assertTrue("parsedQueryStringMap must contain foo[asd].", parsedQueryStringMap.containsKey("foo[asd]"));
        assertTrue("parsedQueryStringMap must have 2 values for foo[asd].",
            parsedQueryStringMap.get("foo[asd]").length == 2);
        assertEquals(FOO_PARAM_VALUES[0], parsedQueryStringMap.get("foo[asd]")[0]);
        assertEquals(FOO_PARAM_VALUES[1], parsedQueryStringMap.get("foo[asd]")[1]);
        assertTrue("parsedQueryStringMap must contain lux.", parsedQueryStringMap.containsKey("lux"));
        assertTrue("parsedQueryStringMap must have 1 value for lux.", parsedQueryStringMap.get("lux").length == 1);
        assertEquals(LUX_PARAM_VALUES[0], parsedQueryStringMap.get("lux")[0]);
        assertFalse(parsedQueryStringMap.containsKey("nameonly"));
    }

    @Test
    public void testParseQueryStringAsSingleValueMap() throws Exception {
        Map<String, Set<String>> parsedQueryStringMap = QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        assertTrue("parsedQueryStringMap must contain foo[asd].", parsedQueryStringMap.containsKey("foo[asd]"));
        assertThat(parsedQueryStringMap.get("foo[asd]")).containsOnly(FOO_PARAM_VALUES);
        assertTrue("parsedQueryStringMap must contain lux.", parsedQueryStringMap.containsKey("lux"));
        assertThat(parsedQueryStringMap.get("lux")).containsOnly(FOO_PARAM_VALUES[0]);
        assertFalse(parsedQueryStringMap.containsKey("nameonly"));
    }

    @Test
    public void testParseQueryStringWithBlankQueryString() throws Exception {
        request.setQueryString(null);
        Map<String, Set<String>> parsedQueryStringMap = QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        assertNotNull(parsedQueryStringMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());
        Map<String, String[]> parsedQueryStringsMap = QueryStringUtils.parseQueryStringAsMultiValuesMap(invokerContext);
        assertNotNull(parsedQueryStringsMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());
        assertFalse(parsedQueryStringMap.containsKey("nameonly"));

        request.setQueryString("");
        parsedQueryStringMap = QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        assertNotNull(parsedQueryStringMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());
        parsedQueryStringsMap = QueryStringUtils.parseQueryStringAsMultiValuesMap(invokerContext);
        assertNotNull(parsedQueryStringsMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());

        request.setQueryString("    ");
        parsedQueryStringMap = QueryStringUtils.parseQueryStringAsSingleValueMap(invokerContext);
        assertNotNull(parsedQueryStringMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());
        parsedQueryStringsMap = QueryStringUtils.parseQueryStringAsMultiValuesMap(invokerContext);
        assertNotNull(parsedQueryStringsMap);
        assertTrue("parsedQueryStringMap must be empty: " + parsedQueryStringMap, parsedQueryStringMap.isEmpty());
    }
}
