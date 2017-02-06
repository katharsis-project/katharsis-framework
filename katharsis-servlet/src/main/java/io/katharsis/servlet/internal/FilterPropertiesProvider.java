package io.katharsis.servlet.internal;

import javax.servlet.FilterConfig;

import io.katharsis.core.internal.boot.PropertiesProvider;

public class FilterPropertiesProvider implements PropertiesProvider {

	private FilterConfig servletConfig;

	public FilterPropertiesProvider(FilterConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public String getProperty(String key) {
		return servletConfig.getInitParameter(key);
	}

}
