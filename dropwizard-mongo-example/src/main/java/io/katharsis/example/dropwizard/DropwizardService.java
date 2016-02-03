package io.katharsis.example.dropwizard;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.katharsis.example.dropwizard.domain.repository.ProjectRepository;
import io.katharsis.example.dropwizard.domain.repository.TaskRepository;
import io.katharsis.example.dropwizard.domain.repository.TaskToProjectRepository;
import io.katharsis.example.dropwizard.managed.MongoManaged;
import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.rs.KatharsisFeature;

import static io.katharsis.rs.KatharsisProperties.RESOURCE_DEFAULT_DOMAIN;
import static io.katharsis.rs.KatharsisProperties.RESOURCE_SEARCH_PACKAGE;

public class DropwizardService extends Application<DropwizardConfiguration> {

    private GuiceBundle<DropwizardConfiguration> guiceBundle;

    @Override
    public void initialize(Bootstrap<DropwizardConfiguration> bootstrap) {

        guiceBundle = GuiceBundle.<DropwizardConfiguration>newBuilder()
                .addModule(new AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(ProjectRepository.class);
                        bind(TaskRepository.class);
                        bind(TaskToProjectRepository.class);
                    }

                    @Provides
                    public MongoManaged mongoManaged(DropwizardConfiguration configuration) throws Exception {
                        return new MongoManaged(configuration.mongo);
                    }
                })
                .setConfigClass(DropwizardConfiguration.class)
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {
        environment.lifecycle().manage(guiceBundle.getInjector().getInstance(MongoManaged.class));


        environment.jersey().property(RESOURCE_SEARCH_PACKAGE, "io.katharsis.example.dropwizard.domain");
        environment.jersey().property(RESOURCE_DEFAULT_DOMAIN, "http://localhost:8080");

        KatharsisFeature katharsisFeature = new KatharsisFeature(environment.getObjectMapper(),
                new QueryParamsBuilder(new DefaultQueryParamsParser()),
                new JsonServiceLocator() {
                    @Override
                    public <T> T getInstance(Class<T> aClass) {
                        return guiceBundle.getInjector().getInstance(aClass);
                    }
                });
        environment.jersey().register(katharsisFeature);
    }

    public static void main(String[] args) throws Exception {
        new DropwizardService().run(args);
    }
}
