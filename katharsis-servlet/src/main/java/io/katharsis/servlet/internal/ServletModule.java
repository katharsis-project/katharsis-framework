package io.katharsis.servlet.internal;

import javax.servlet.http.HttpServletRequest;

import io.katharsis.module.Module;

public class ServletModule implements Module {

	private ThreadLocal<HttpServletRequest> requestThreadLocal;

	public ServletModule(ThreadLocal<HttpServletRequest> requestThreadLocal) {
		this.requestThreadLocal = requestThreadLocal;
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addSecurityProvider(new ServletSecurityProvider(requestThreadLocal));
	}

	@Override
	public String getModuleName() {
		return "servlet";
	}
}
