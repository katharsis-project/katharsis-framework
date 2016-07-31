package io.katharsis.dispatcher.handlers;

import io.katharsis.dispatcher.DefaultResponseContext;
import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.dispatcher.registry.annotated.AnnotatedResourceRepositoryAdapter;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.domain.CollectionResponse;
import io.katharsis.domain.SingleResponse;
import io.katharsis.domain.api.TopLevel;
import io.katharsis.query.QueryParams;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static io.katharsis.request.Request.JsonApiRequestType.COLLECTION_IDS;

/**
 * Implements JSON-API spec related to etching data.
 * <p/>
 * http://jsonapi.org/format/#fetching
 */
@Data
@Slf4j
public class JsonApiGet implements JsonApiHandler {

    private final RepositoryRegistry registry;

    @Override
    public ResponseContext handle(Request req) {

        ResponseContext res = null;
        switch (req.requestType()) {
            case COLLECTION:
            case COLLECTION_IDS:
                // find repository for resource and call method
                res = handleCollectionFetch(req);
                break;
            case SINGLE_RESOURCE:
                res = handleSingleResourceFetch(req);
                break;
            case FIELD:
                // validate field is relationship, fetch repository for it and return the field (collection/single)
                res = handleFieldFetch(req);
                break;
            case RELATIONSHIP:
                // validate relationship, fetch repository and return resource id('s)
                res = handleRelationshipFetch(req);
                break;
            default:
                throw new IllegalStateException("Unknown state " + req.requestType());
        }

        return res;
    }

    private ResponseContext handleFieldFetch(Request req) {
        return null;
    }

    private ResponseContext handleRelationshipFetch(Request req) {
        JsonApiPath path = req.getPath();
        QueryParams queryParams = new QueryParams(DefaultQueryParamsParser.splitQuery(path.getQuery().orElse("")));

        // TODO: ieugen: fetch a relationship repository
//        AnnotatedResourceRepositoryAdapter repository = registry.get(path.getResource());
//
//        Object response = repository.findOne(req.getParameterProvider(), path.getIds().get().get(0), queryParams);
//
//        if (response instanceof ResponseContext) {
//            return (ResponseContext) response;
//        }
//
//        return new DefaultResponseContext(200, new SingleResponse(response, null, null, null, null));

        return new DefaultResponseContext(200, new SingleResponse(null, null, null, null, null));
    }

    private ResponseContext handleSingleResourceFetch(Request req) {
        JsonApiPath path = req.getPath();
        QueryParams queryParams = new QueryParams(DefaultQueryParamsParser.splitQuery(path.getQuery().orElse("")));

        AnnotatedResourceRepositoryAdapter repository = registry.get(path.getResource());

        Object response = repository.findOne(req.getParameterProvider(), path.getIds().get().get(0), queryParams);
        if (response instanceof ResponseContext) {
            return (ResponseContext) response;
        }

        return new DefaultResponseContext(200, new SingleResponse(response, null, null, null, null));
    }

    private ResponseContext handleCollectionFetch(Request req) {
        JsonApiPath path = req.getPath();

        AnnotatedResourceRepositoryAdapter repository = registry.get(path.getResource());

        QueryParams queryParams = new QueryParams(DefaultQueryParamsParser.splitQuery(path.getQuery().orElse("")));
        // we could get a resource or a list of id's
        Object response;
        if (req.requestType() == COLLECTION_IDS) {
            response = repository.findAll(req.getParameterProvider(), path.getIds().get(), queryParams);
        } else {
            response = repository.findAll(req.getParameterProvider(), queryParams);
        }

        if (response instanceof ResponseContext) {
            return (ResponseContext) response;
        }

        TopLevel document = null;
        if (response instanceof Iterable) {
            document = new CollectionResponse((Iterable) response, null, null, null, null);
        } else if (response instanceof TopLevel) {
            document = (TopLevel) response;
        }
        return new DefaultResponseContext(200, document);
    }


}
