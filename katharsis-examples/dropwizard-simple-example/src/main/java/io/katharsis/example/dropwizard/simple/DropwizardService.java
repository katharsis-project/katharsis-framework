package io.katharsis.example.dropwizard.simple;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.katharsis.core.properties.KatharsisProperties;
import io.katharsis.rs.KatharsisFeature;

public class DropwizardService extends Application<DropwizardConfiguration> {

	@Override
	public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {
		environment.jersey().property(KatharsisProperties.RESOURCE_SEARCH_PACKAGE, dropwizardConfiguration.katharsis.searchPackage);

		KatharsisFeature katharsisFeature = new KatharsisFeature();
		environment.jersey().register(katharsisFeature);
	}

	public static void main(String[] args) throws Exception {
		new DropwizardService().run(args);
	}
}
