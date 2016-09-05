package io.katharsis.example.dropwizardSimple;

import io.dropwizard.Configuration;
import io.katharsis.example.dropwizardSimple.domain.model.Project;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DropwizardConfiguration extends Configuration {

    @Valid
    @NotNull
    public KatharsisConfiguration katharsis = new KatharsisConfiguration();

    public class KatharsisConfiguration {

        @Valid
        @NotNull
        public String searchPackage = Project.class.getPackage().getName();
    }
}
