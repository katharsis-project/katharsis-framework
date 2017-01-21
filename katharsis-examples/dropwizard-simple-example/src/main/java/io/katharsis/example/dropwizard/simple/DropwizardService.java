package io.katharsis.example.dropwizard.simple;

import static io.katharsis.rs.KatharsisProperties.RESOURCE_SEARCH_PACKAGE;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.queryspec.DefaultQuerySpecDeserializer;
import io.katharsis.rs.KatharsisFeature;

public class DropwizardService extends Application<DropwizardConfiguration> {

    @Override
    public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {


        environment.jersey().property(RESOURCE_SEARCH_PACKAGE, dropwizardConfiguration.katharsis.searchPackage);

        KatharsisFeature katharsisFeature = new KatharsisFeature(environment.getObjectMapper(),
                new DefaultQuerySpecDeserializer(),
                new SampleJsonServiceLocator());
        environment.jersey().register(katharsisFeature);
    }

    public static void main(String[] args) throws Exception {
        new DropwizardService().run(args);
    }
}
