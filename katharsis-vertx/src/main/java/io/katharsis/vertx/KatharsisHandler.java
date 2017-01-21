package io.katharsis.vertx;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Vertx handler to Katharsis resource controller. Vertx delegates request processing to Katharsis controller.
 */
@Slf4j
@Value
@RequiredArgsConstructor
public class KatharsisHandler implements Handler<RoutingContext> {

    private final ObjectMapper mapper;
    private final String webPath;
    private final PathBuilder pathBuilder;
    private final ParameterProviderFactory parameterProviderFactory;
    private final RequestDispatcher requestDispatcher;

    @Override
    public void handle(RoutingContext ctx) {

        JsonPath jsonPath = buildPath(ctx);
        String requestMethod = ctx.request().method().name();

        Map<String, Set<String>> parameters = getParameters(ctx);

        RepositoryMethodParameterProvider provider = parameterProviderFactory.provider(ctx);
        Document body = requestBody(ctx.getBodyAsString());

        try {
            Response response = requestDispatcher.dispatchRequest(jsonPath, requestMethod, parameters, provider, body);

            ctx.response()
                    .setStatusCode(response.getHttpStatus())
                    .putHeader(HttpHeaders.CONTENT_TYPE, JsonApiMediaTypeHandler.APPLICATION_JSON_API)
                    .end(encode(response.getDocument()));

        } catch (Exception e) {
            throw new KatharsisVertxException("Exception during dispatch " + e.getMessage());
        }
    }

    protected JsonPath buildPath(RoutingContext ctx) {
        return buildPath(ctx.request().path());
    }

    protected JsonPath buildPath(@NonNull String path) {
        //TODO: ieugen path need to be cleaned
        String cleaned = Paths.get(path).toString().replace('\\', '/');
        String transformed = cleaned.substring(webPath.length());
        log.trace("Path is {}", transformed);
        return pathBuilder.buildPath(transformed);
    }

    protected Map<String, Set<String>> getParameters(RoutingContext ctx) {
        Map<String, Set<String>> transformed = new HashMap<>();

        QueryStringDecoder decoder = new QueryStringDecoder(ctx.request().uri());

        for (Map.Entry<String, List<String>> param : decoder.parameters().entrySet()) {
            transformed.put(param.getKey(), new HashSet<>(param.getValue()));
        }
        return transformed;
    }

    protected Document requestBody(String body) {
        if (body == null || body.length() == 0) {
            return null;
        }
        return decodeValue(body, Document.class);
    }

    /**
     * Taken from io.vertx.json.Json.
     */
    protected <T> T decodeValue(String str, Class<T> clazz) throws DecodeException {
        try {
            return mapper.readValue(str, clazz);
        } catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage());
        }
    }

    /**
     * Taken from io.vertx.json.Json.
     */
    protected String encode(Object obj) throws EncodeException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

}
