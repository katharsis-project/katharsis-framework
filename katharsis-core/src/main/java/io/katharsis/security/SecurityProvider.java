package io.katharsis.security;

public interface SecurityProvider {

	public boolean isUserInRole(String role);
}
