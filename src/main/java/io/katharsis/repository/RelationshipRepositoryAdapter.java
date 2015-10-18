package io.katharsis.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.annotations.JsonApiSetRelation;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
    implements RelationshipRepository<T, T_ID, D, D_ID> {

    private final Object implementationObject;
    private final ParametersFactory parametersFactory;

    private Method setRelationMethod;
    private Method setRelationsMethod;
    private Method addRelationsMethod;
    private Method removeRelationsMethod;
    private Method findOneTargetMethod;
    private Method findManyTargetsMethod;

    public RelationshipRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        this.implementationObject = implementationObject;
        this.parametersFactory = parametersFactory;
    }

    @Override
    public void setRelation(T source, D_ID targetId, String fieldName) {
        Class<JsonApiSetRelation> annotationType = JsonApiSetRelation.class;
        if (setRelationMethod == null) {
            setRelationMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, setRelationMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(id, setRelationMethod.getParameters(), requestParams, annotationType);

        try {
            setRelationMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName) {

    }

    @Override
    public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName) {

    }

    @Override
    public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName) {

    }

    @Override
    public D findOneTarget(T_ID sourceId, String fieldName, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<D> findManyTargets(T_ID sourceId, String fieldName, RequestParams requestParams) {
        return null;
    }

    private void checkIfNotNull(Class<? extends Annotation> annotationClass, Method foundMethod) {
        if (foundMethod == null) {
            throw new RepositoryAnnotationNotFoundException(
                String.format("Annotation %s for class %s not found", annotationClass, implementationObject.getClass()));
        }
    }
}
