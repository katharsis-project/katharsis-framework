package io.katharsis.servlet.internal;

import javax.servlet.ServletConfig;

import io.katharsis.core.internal.boot.PropertiesProvider;

public class ServletPropertiesProvider implements PropertiesProvider {

	private ServletConfig servletConfig;

	public ServletPropertiesProvider(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public String getProperty(String key) {
		return servletConfig.getInitParameter(key);
	}

}
