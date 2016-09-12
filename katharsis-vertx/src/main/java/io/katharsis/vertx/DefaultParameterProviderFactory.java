package io.katharsis.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameter Factory - injects objects from the request context in the repository method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultParameterProviderFactory implements ParameterProviderFactory {

    private ObjectMapper mapper;

    @Override
    public RepositoryMethodParameterProvider provider(RoutingContext ctx) {
        return new DefaultParameterProvider(mapper, ctx);
    }

}
