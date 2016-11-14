package io.katharsis.example.dropwizard.simple;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class DropwizardConfiguration extends Configuration {

    @Valid
    @NotNull
    public KatharsisConfiguration katharsis = new KatharsisConfiguration();

    public class KatharsisConfiguration {

        @Valid
        @NotNull
        public String searchPackage = "io.katharsis.example.dropwizard.simple.domain";
    }
}
