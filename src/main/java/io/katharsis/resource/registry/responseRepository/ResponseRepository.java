package io.katharsis.resource.registry.responseRepository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.LinksRepository;
import io.katharsis.repository.MetaRepository;
import io.katharsis.repository.annotated.AnnotatedRepositoryAdapter;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;

import java.util.Collections;

/**
 * The adapter is used to create a common layer between controllers and repositories. Every repository can return either
 * a resource object or a {@link JsonApiResponse} response which should be returned by a controller. Ok, the last
 * sentence is not 100% true since interface based repositories can return only resources, but who's using it anyway?
 *
 * The methods need to know if a repository is interface- or annotation-based since repository methods have different
 * signatures.
 */
public abstract class ResponseRepository {

    protected JsonApiResponse getResponse(Object repository, Object resource, QueryParams queryParams) {
        if (resource instanceof JsonApiResponse) {
            return (JsonApiResponse) resource;
        }

        Iterable resources;
        if (resource instanceof Iterable) {
            resources = (Iterable) resource;
        } else {
            resources = Collections.singletonList(resource);
        }
        MetaInformation metaInformation = getMetaInformation(repository, resources, queryParams);
        LinksInformation linksInformation = getLinksInformation(repository, resources, queryParams);

        return new JsonApiResponse()
            .setEntity(resource)
            .setLinksInformation(linksInformation)
            .setMetaInformation(metaInformation);
    }

    @SuppressWarnings("unchecked")
    private MetaInformation getMetaInformation(Object repository, Iterable<?> resources, QueryParams queryParams) {
        if (repository instanceof AnnotatedRepositoryAdapter) {
            if (((AnnotatedRepositoryAdapter) repository).metaRepositoryAvailable()) {
                return ((MetaRepository) repository).getMetaInformation(resources, queryParams);
            }
        } else if (repository instanceof MetaRepository) {
            return ((MetaRepository) repository).getMetaInformation(resources, queryParams);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private LinksInformation getLinksInformation(Object repository, Iterable<?> resources, QueryParams queryParams) {
        if (repository instanceof AnnotatedRepositoryAdapter) {
            if (((AnnotatedRepositoryAdapter) repository).linksRepositoryAvailable()) {
                return ((LinksRepository) repository).getLinksInformation(resources, queryParams);
            }
        } else if (repository instanceof LinksRepository) {
            return ((LinksRepository) repository).getLinksInformation(resources, queryParams);
        }
        return null;
    }
}
