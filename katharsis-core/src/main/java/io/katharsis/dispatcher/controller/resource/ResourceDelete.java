package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.controller.HttpMethod;
import io.katharsis.dispatcher.controller.Utils;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.responseRepository.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.parser.TypeParser;

public class ResourceDelete extends BaseController {

    private final ResourceRegistry resourceRegistry;
    private final TypeParser typeParser;
    private final QueryParamsBuilder queryParamsBuilder;

    public ResourceDelete(ResourceRegistry resourceRegistry,
                          TypeParser typeParser,
                          QueryParamsBuilder paramsBuilder,
                          ObjectMapper objectMapper) {
        super(objectMapper);
        this.resourceRegistry = resourceRegistry;
        this.typeParser = typeParser;
        this.queryParamsBuilder = paramsBuilder;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if requested resource method is acceptable - is a DELETE request for a resource.
     */
    @Override
    public boolean isAcceptable(Request request) {
        return request.getMethod() == HttpMethod.DELETE && canDelete(request.getPath());
    }

    /**
     * Can delete only for URLs like:
     * - http://host.local/tasks/1
     * - http://host.local/tasks/1/relationships/project
     *
     * @return
     */

    protected boolean canDelete(JsonApiPath path) {
        if (path.isCollection()) {
            return false;
        }
        if (path.getIds().isPresent()) {
            if (path.getIds().get().size() > 1) {
                return false;
            }
            if (path.getField().isPresent()) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public BaseResponseContext handle(Request request) {
        JsonApiPath path = request.getPath();

        RegistryEntry registryEntry = resourceRegistry.getEntry(path.getResource());
        Utils.checkResourceExists(registryEntry, path.getResource());

        ResourceRepositoryAdapter repository = registryEntry.getResourceRepository(request.getParameterProvider());

        QueryParams queryParams = getQueryParamsBuilder().parseQuery(request.getQuery());

        for (String id : path.getIds().get()) {
            repository.delete(parseId(registryEntry, id), queryParams);
        }

        // TODO: ieugen: return list of deleted items ??
        return null;
    }

    @Override
    public TypeParser getTypeParser() {
        return typeParser;
    }

    @Override
    public QueryParamsBuilder getQueryParamsBuilder() {
        return queryParamsBuilder;
    }

}
