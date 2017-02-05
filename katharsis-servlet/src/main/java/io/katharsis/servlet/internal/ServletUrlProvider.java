package io.katharsis.servlet.internal;

import javax.servlet.http.HttpServletRequest;

import io.katharsis.resource.registry.ServiceUrlProvider;

public class ServletUrlProvider implements ServiceUrlProvider {

	private ThreadLocal<HttpServletRequest> requestThreadLocal;

	public ServletUrlProvider(ThreadLocal<HttpServletRequest> requestThreadLocal) {
		this.requestThreadLocal = requestThreadLocal;
	}

	@Override
	public String getUrl() {
		HttpServletRequest request = requestThreadLocal.get();
		if (request == null) {
			throw new IllegalStateException("uriInfo not available, make sure to call onRequestStarted in advance");
		}

		String requestUrl = request.getRequestURL().toString();
		String servletPath = request.getServletPath();

		int sep = requestUrl.indexOf(servletPath);
		String url = requestUrl.substring(0, sep + servletPath.length());
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		} else {
			return url;
		}
	}
}
