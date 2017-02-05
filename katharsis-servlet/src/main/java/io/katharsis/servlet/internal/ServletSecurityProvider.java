package io.katharsis.servlet.internal;

import javax.servlet.http.HttpServletRequest;

import io.katharsis.security.SecurityProvider;

public class ServletSecurityProvider implements SecurityProvider {

	private ThreadLocal<HttpServletRequest> requestThreadLocal;

	public ServletSecurityProvider(ThreadLocal<HttpServletRequest> requestThreadLocal) {
		this.requestThreadLocal = requestThreadLocal;
	}

	@Override
	public boolean isUserInRole(String role) {
		HttpServletRequest request = requestThreadLocal.get();
		return request.isUserInRole(role);
	}

}
