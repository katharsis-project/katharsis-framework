package io.katharsis.dispatcher.registry;

import io.katharsis.dispatcher.registry.annotated.AnnotatedRelationshipRepositoryAdapter;
import io.katharsis.dispatcher.registry.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.dispatcher.registry.annotated.ParametersFactory;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.locator.NewInstanceRepositoryFactory;
import io.katharsis.locator.RepositoryFactory;
import io.katharsis.repository.exception.RepositoryNotFoundException;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class RepositoryRegistryImpl implements RepositoryRegistry {

    private Map<String, AnnotatedResourceRepositoryAdapter> adapters;
    private Map<String, Map<String, AnnotatedRelationshipRepositoryAdapter>> relationshipRepoAdapters;

    private String packages;
    private String apiMountUrl;

    public RepositoryRegistryImpl(@NonNull String apiMountUrl,
                                  @NonNull Map<String, AnnotatedResourceRepositoryAdapter> adapters,
                                  @NonNull Map<String, Map<String, AnnotatedRelationshipRepositoryAdapter>> relationshipAdapters) {
        this.apiMountUrl = apiMountUrl;
        this.adapters = adapters;
        this.relationshipRepoAdapters = relationshipAdapters;
    }

    public static RepositoryRegistryImpl build(String packages, String apiMountUrl) {
        DefaultResourceLookup resourceLookup = new DefaultResourceLookup();
        ResourceRegistry holder = resourceLookup.scan(packages.split(","));

        RepositoryFactory factory = new NewInstanceRepositoryFactory(new ParametersFactory());

        Map<String, AnnotatedResourceRepositoryAdapter> adapters = buildAdapters(factory, holder.getRepositories());
        Map<String, Map<String, AnnotatedRelationshipRepositoryAdapter>> relationshipAdapters =
                buildRelationshipAdapters();

        return new RepositoryRegistryImpl(apiMountUrl, adapters, relationshipAdapters);
    }

    private static Map<String, Map<String, AnnotatedRelationshipRepositoryAdapter>> buildRelationshipAdapters() {
        //TODO: ieugen: we must implement this to have relationships
        return new HashMap<>();
    }

    private static Map<String, AnnotatedResourceRepositoryAdapter> buildAdapters(RepositoryFactory factory,
                                                                                 Map<String, Class<?>> repositories) {

        Map<String, AnnotatedResourceRepositoryAdapter> adapters = new HashMap<>();

        for (Map.Entry<String, Class<?>> entry : repositories.entrySet()) {
            AnnotatedResourceRepositoryAdapter adapter = (AnnotatedResourceRepositoryAdapter) factory.build(entry.getValue());
            adapters.put(entry.getKey(), adapter);
        }
        return adapters;
    }

    @Override
    public AnnotatedResourceRepositoryAdapter get(String resource) throws RepositoryNotFoundException {
        if (!adapters.containsKey(resource)) {
            throw new RepositoryNotFoundException(resource);
        }
        return adapters.get(resource);
    }
}
