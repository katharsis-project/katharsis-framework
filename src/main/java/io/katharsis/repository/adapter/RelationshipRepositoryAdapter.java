package io.katharsis.repository.adapter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.annotations.*;
import io.katharsis.repository.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
    extends RepositoryAdapter<T>
    implements RelationshipRepository<T, T_ID, D, D_ID> {

    private Method setRelationMethod;
    private Method setRelationsMethod;
    private Method addRelationsMethod;
    private Method removeRelationsMethod;
    private Method findOneTargetMethod;
    private Method findManyTargetsMethod;

    public RelationshipRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
    }

    @Override
    public void setRelation(T source, D_ID targetId, String fieldName) {
        Class<JsonApiSetRelation> annotationType = JsonApiSetRelation.class;
        if (setRelationMethod == null) {
            setRelationMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Method method = this.setRelationMethod;
        Object[] firstParameters = {source, targetId, fieldName};
        performRelationChangeOperation(annotationType, method, firstParameters);
    }

    @Override
    public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
        Class<JsonApiSetRelations> annotationType = JsonApiSetRelations.class;
        if (setRelationsMethod == null) {
            setRelationsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Method method = this.setRelationsMethod;
        Object[] firstParameters = {source, targetIds, fieldName};
        performRelationChangeOperation(annotationType, method, firstParameters);
    }

    @Override
    public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
        Class<JsonApiAddRelations> annotationType = JsonApiAddRelations.class;
        if (addRelationsMethod == null) {
            addRelationsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Method method = this.addRelationsMethod;
        Object[] firstParameters = {source, targetIds, fieldName};
        performRelationChangeOperation(annotationType, method, firstParameters);
    }

    @Override
    public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName) {
        Class<JsonApiRemoveRelations> annotationType = JsonApiRemoveRelations.class;
        if (removeRelationsMethod == null) {
            removeRelationsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Method method = this.removeRelationsMethod;
        Object[] firstParameters = {source, targetIds, fieldName};
        performRelationChangeOperation(annotationType, method, firstParameters);
    }

    private void performRelationChangeOperation(Class<? extends Annotation> annotationType, Method method, Object[] firstParameters) {
        checkIfNotNull(annotationType, method);

        Object[] methodParameters = parametersFactory
            .buildParameters(firstParameters, method.getParameters(), annotationType);

        try {
            method.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public D findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Class<JsonApiFindOneTarget> annotationType = JsonApiFindOneTarget.class;
        if (findOneTargetMethod == null) {
            findOneTargetMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findOneTargetMethod);

        Object[] firstParameters = {sourceId, fieldName};
        Object[] methodParameters = parametersFactory
            .buildParameters(firstParameters, findOneTargetMethod.getParameters(), queryParams, annotationType);

        try {
            return (D) findOneTargetMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }

    @Override
    public Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Class<JsonApiFindManyTargets> annotationType = JsonApiFindManyTargets.class;
        if (findManyTargetsMethod == null) {
            findManyTargetsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        checkIfNotNull(annotationType, findManyTargetsMethod);

        Object[] firstParameters = {sourceId, fieldName};
        Object[] methodParameters = parametersFactory
            .buildParameters(firstParameters, findManyTargetsMethod.getParameters(), queryParams, annotationType);

        try {
            return (Iterable<D>) findManyTargetsMethod.invoke(implementationObject, methodParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw (RuntimeException)e.getCause();
        }
    }
}
