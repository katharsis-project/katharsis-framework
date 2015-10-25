package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JaxRsParameterProviderTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ContainerRequestContext requestContext;

    private JaxRsParameterProvider sut;

    @Before
    public void setUp() throws Exception {
        sut = new JaxRsParameterProvider(objectMapper, requestContext);
    }

    @Test
    public void onContainerRequestContextParameterShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(0);

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        assertThat(result).isEqualTo(requestContext);
    }

    @Test
    public void onSecurityContextParameterShouldReturnThisInstance() throws Exception {
        // GIVEN
        SecurityContext securityContext = mock(SecurityContext.class);
        Parameter parameter = getParameter(1);
        when(requestContext.getSecurityContext()).thenReturn(securityContext);

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getSecurityContext();
        assertThat(result).isEqualTo(securityContext);
    }

    @Test
    public void onObjectCookieShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(2);
        Cookie cookie = new Cookie("sid", "123");
        when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", cookie));

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getCookies();
        assertThat(result).isEqualTo(cookie);
    }

    @Test
    public void onStringCookieShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(3);
        when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", new Cookie("sid", "123")));

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getCookies();
        assertThat(result).isEqualTo("123");
    }

    @Test
    public void onLongCookieShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(4);
        when(requestContext.getCookies()).thenReturn(Collections.singletonMap("sid", new Cookie("sid", "123")));
        when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(123L);

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getCookies();
        verify(objectMapper).readValue("123", Long.class);
        assertThat(result).isEqualTo(123L);
    }

    @Test
    public void onStringHeaderShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(5);
        UUID uuid = UUID.randomUUID();
        when(requestContext.getHeaderString(any())).thenReturn(uuid.toString());

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getHeaderString("cid");
        assertThat(result).isEqualTo(uuid.toString());
    }

    @Test
    public void onUuidHeaderShouldReturnThisInstance() throws Exception {
        // GIVEN
        Parameter parameter = getParameter(6);
        UUID uuid = UUID.randomUUID();
        when(requestContext.getHeaderString(any())).thenReturn(uuid.toString());
        when(objectMapper.readValue(any(String.class), any(Class.class))).thenReturn(uuid);

        // WHEN
        Object result = sut.provide(parameter);

        // THEN
        verify(requestContext).getHeaderString("cid");
        verify(objectMapper).readValue(uuid.toString(), UUID.class);
        assertThat(result).isEqualTo(uuid);
    }

    private Parameter getParameter(int idx) {
        return TestClass.class.getDeclaredMethods()[0].getParameters()[idx];
    }

    public static class TestClass {
        public void testMethod(ContainerRequestContext requestContext, SecurityContext securityContext,
                               @CookieParam("sid") Cookie objectCookie, @CookieParam("sid") String StringCookie,
                               @CookieParam("sid") Long longCookie, @HeaderParam("cid") String StringHeader,
                               @HeaderParam("cid") UUID UuidHeader) {}
    }
}
