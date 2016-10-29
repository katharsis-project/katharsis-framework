package io.katharsis.rs.internal;

import javax.ws.rs.core.SecurityContext;

import io.katharsis.security.SecurityProvider;

public class JaxrsSecurityProvider implements SecurityProvider {

	private SecurityContext context;

	public JaxrsSecurityProvider(SecurityContext context) {
		this.context = context;
	}

	@Override
	public boolean isUserInRole(String role) {
		return context.isUserInRole(role);
	}
}
