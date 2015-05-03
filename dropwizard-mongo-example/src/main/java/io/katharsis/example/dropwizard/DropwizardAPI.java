package io.katharsis.example.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.katharsis.example.dropwizard.managed.MongoManaged;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.rs.KatharsisFeature;

import static io.katharsis.rs.KatharsisProperties.RESOURCE_DEFAULT_DOMAIN;
import static io.katharsis.rs.KatharsisProperties.RESOURCE_SEARCH_PACKAGE;

public class DropwizardAPI extends Application<DropwizardConfiguration> {
    @Override
    public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {
        MongoManaged mongoManaged = new MongoManaged(dropwizardConfiguration.mongo);
        environment.lifecycle().manage(mongoManaged);

        environment.jersey().property(RESOURCE_SEARCH_PACKAGE, "io.katharsis.example.dropwizard.domain");
        environment.jersey().property(RESOURCE_DEFAULT_DOMAIN, "http://test.local");

        KatharsisFeature katharsisFeature = new KatharsisFeature(environment.getObjectMapper(), new SampleJsonServiceLocator());
        environment.jersey().register(katharsisFeature);
    }

    public static void main(String[] args) throws Exception {
        new DropwizardAPI().run(args);
    }
}
