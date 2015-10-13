package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.annotations.Delete;
import io.katharsis.repository.annotations.FindAll;
import io.katharsis.repository.annotations.FindOne;
import io.katharsis.repository.annotations.Save;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.repository.exception.RepositoryMethodException;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class ResourceRepositoryFacade<T, ID extends Serializable> implements ResourceRepository<T, ID> {
    private final Object implementationObject;
    private final ResourceMethodParameterProvider parameterProvider;

    private Method findOneMethod;
    private Method findAllMethod;
    private Method saveMethod;
    private Method deleteMethod;

    public ResourceRepositoryFacade(Object implementationObject, ResourceMethodParameterProvider parameterProvider) {
        this.implementationObject = implementationObject;
        this.parameterProvider = parameterProvider;
    }

    @Override
    public T findOne(ID id, RequestParams requestParams) {
        Class<FindOne> annotationType = FindOne.class;
        if (findOneMethod == null) {
            findOneMethod = findMethodWith(annotationType);
        }
        checkIfNotNull(annotationType, findOneMethod);

        Object[] methodParameters = buildParameters(id, requestParams, annotationType);

        try {
            return (T) findOneMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<T> findAll(RequestParams requestParams) {
        if (findAllMethod == null) {
            findAllMethod = findMethodWith(FindAll.class);
        }
        checkIfNotNull(FindAll.class, findAllMethod);

        Parameter[] parametersToResolve = findAllMethod.getParameters();
        Object[] methodParameters = resolveParameters(parametersToResolve, requestParams);

        try {
            return (Iterable<T>) findAllMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        Class<Save> annotationType = Save.class;
        if (saveMethod == null) {
            saveMethod = findMethodWith(annotationType);
        }
        checkIfNotNull(annotationType, saveMethod);

        Object[] methodParameters = buildParameters(entity, saveMethod.getParameters(), annotationType);

        try {
            return (S) saveMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(ID id) {
        Class<Delete> annotationType = Delete.class;
        if (deleteMethod == null) {
            deleteMethod = findMethodWith(annotationType);
        }
        checkIfNotNull(annotationType, deleteMethod);

        Object[] methodParameters = buildParameters(id, deleteMethod.getParameters(), annotationType);

        try {
            deleteMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] buildParameters(Object firstParameter, RequestParams requestParams, Class<? extends Annotation> annotationType) {
        Parameter[] parameters = findOneMethod.getParameters();
        if (parameters.length < 1) {
            throw new RepositoryMethodException(String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, 1, parameters.length);
        Object[] additionalParameters = resolveParameters(parametersToResolve, requestParams);

        return prepend(firstParameter, additionalParameters);
    }

    private Object[] buildParameters(Object firstParameter, Parameter[] parameters, Class<? extends Annotation> annotationType) {
        if (parameters.length < 1) {
            throw new RepositoryMethodException(String.format("Method with %s annotation should have at least one parameter.", annotationType));
        }
        Parameter[] parametersToResolve = Arrays.copyOfRange(parameters, 1, parameters.length);
        Object[] additionalParameters = resolveParameters(parametersToResolve);

        return prepend(firstParameter, additionalParameters);
    }

    private Method findMethodWith(Class<? extends Annotation> annotationClass) {
        Method foundMethod = null;
        Class<?> currentClass = implementationObject.getClass();
        methodFinder:
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass) && !method.isSynthetic()) {
                    foundMethod = method;
                    break methodFinder;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return foundMethod;
    }

    private Object[] resolveParameters(Parameter[] parametersToResolve, RequestParams requestParams) {
        Object[] parameterValues = new Object[parametersToResolve.length];
        for (int i = 0; i < parametersToResolve.length; i++) {
            Parameter parameter = parametersToResolve[i];
            if (RequestParams.class.equals(parameter.getType())) {
                parameterValues[i] = requestParams;
            } else {
                parameterValues[i] = parameterProvider.provide(parameter);
            }
        }
        return parameterValues;
    }

    private Object[] resolveParameters(Parameter[] parametersToResolve) {
        Object[] parameterValues = new Object[parametersToResolve.length];
        for (int i = 0; i < parametersToResolve.length; i++) {
            parameterValues[i] = parameterProvider.provide(parametersToResolve[i]);
        }
        return parameterValues;
    }

    private void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }

    public Object[] prepend(Object prefix, Object[] oldArray) {
        Object[] newArray = new Object[oldArray.length + 1];
        System.arraycopy(oldArray, 0, newArray, 1, oldArray.length);
        newArray[0] = prefix;

        return newArray;
    }
}
