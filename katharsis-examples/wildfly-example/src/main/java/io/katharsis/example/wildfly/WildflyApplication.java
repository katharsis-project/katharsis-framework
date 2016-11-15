package io.katharsis.example.wildfly;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.katharsis.rs.KatharsisFeature;

@ApplicationPath("/")
public class WildflyApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<>();
		set.add(KatharsisFeature.class);
		return set;
	}
}
