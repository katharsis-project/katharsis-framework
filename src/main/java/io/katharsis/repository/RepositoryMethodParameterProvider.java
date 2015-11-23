package io.katharsis.repository;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Provides additional parameters for an annotated repository method. The method can accept more than the required
 * parameters for a functionality. For example:
 * <pre>
 * {@code
 *  &#64;JsonApiFindOne
 *  public Resource findOne(Long id) {
 *    ...
 *  }
 *  }
 * </pre>
 * This method has {@link io.katharsis.repository.annotations.JsonApiFindOne} annotation which require the first
 * parameter to be a resource identifier to be found. However, it is not the only parameter that can be defined.
 * It's possible to pass additional, web framework dependant objects associated with a request. When using JAX-RS
 * integration, it's possible to pass <b>javax.ws.rs.core.SecurityContext</b>. To allow doing that, JAX-RS adapter
 * has implemented {@link RepositoryMethodParameterProvider} to pass several framework classes to the repository method.
 * An example below shows a sample repository which makes use of JAX-RS integration:
 * <pre>
 * {@code
 *  &#64;JsonApiFindOne
 *  public Resource findOne(Long id, @HeaderParam("X-Token") String auth Token, SecurityContext securityContext) {
 *    ...
 *  }
 *  }
 * </pre>
 * <p>
 *     This interface has to be implemented for every Katharsis web framework integration.
 * </p>
 */
public interface RepositoryMethodParameterProvider {

    /**
     * Return an instance of a custom parameter.
     * @param method repository method which contain the parameter
     * @param parameterIndex  index of the parameter in the method parameters
     * @param <T> Type of a parameter
     * @return parameter value or null if not found
     */
    <T> T provide(Method method, int parameterIndex);

    /**
     * An util method to extract a parameter object.
     * @param method parameter's method
     * @param parameterIndex index of the parameter
     * @return parameter object
     */
    default Parameter getParameter(Method method, int parameterIndex) {
        return method.getParameters()[parameterIndex];
    }
}
