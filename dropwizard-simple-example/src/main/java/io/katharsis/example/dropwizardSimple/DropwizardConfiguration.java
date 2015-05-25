package io.katharsis.example.dropwizardSimple;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DropwizardConfiguration extends Configuration {

    @Valid
    @NotNull
    public KatharsisConfiguration katharsis = new KatharsisConfiguration();

    public class KatharsisConfiguration {

        @Valid
        @NotNull
        public String host;

        @Valid
        @NotNull
        public String searchPackage;
    }
}
