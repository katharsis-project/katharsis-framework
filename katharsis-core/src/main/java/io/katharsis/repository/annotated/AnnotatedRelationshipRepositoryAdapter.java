package io.katharsis.repository.annotated;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.annotations.JsonApiAddRelations;
import io.katharsis.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.repository.annotations.JsonApiFindOneTarget;
import io.katharsis.repository.annotations.JsonApiRemoveRelations;
import io.katharsis.repository.annotations.JsonApiSetRelation;
import io.katharsis.repository.annotations.JsonApiSetRelations;
import io.katharsis.utils.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

public class AnnotatedRelationshipRepositoryAdapter<T, T_ID extends Serializable, D, D_ID extends Serializable>
    extends AnnotatedRepositoryAdapter<T>{

    private Method setRelationMethod;
    private Method setRelationsMethod;
    private Method addRelationsMethod;
    private Method removeRelationsMethod;
    private Method findOneTargetMethod;
    private Method findManyTargetsMethod;

    public AnnotatedRelationshipRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
    }

    public void setRelation(T source, D_ID targetId, String fieldName, QueryParams queryParams) {
        Class<JsonApiSetRelation> annotationType = JsonApiSetRelation.class;
        if (setRelationMethod == null) {
            setRelationMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(setRelationMethod, annotationType, new Object[]{source, targetId, fieldName}, queryParams);
    }

    public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        Class<JsonApiSetRelations> annotationType = JsonApiSetRelations.class;
        if (setRelationsMethod == null) {
            setRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(setRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        Class<JsonApiAddRelations> annotationType = JsonApiAddRelations.class;
        if (addRelationsMethod == null) {
            addRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(addRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        Class<JsonApiRemoveRelations> annotationType = JsonApiRemoveRelations.class;
        if (removeRelationsMethod == null) {
            removeRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(removeRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryParams);
    }

    public Object findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Class<JsonApiFindOneTarget> annotationType = JsonApiFindOneTarget.class;
        if (findOneTargetMethod == null) {
            findOneTargetMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findOneTargetMethod, annotationType, new Object[]{sourceId, fieldName}, queryParams);
    }

    public Object findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams) {
        Class<JsonApiFindManyTargets> annotationType = JsonApiFindManyTargets.class;
        if (findManyTargetsMethod == null) {
            findManyTargetsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findManyTargetsMethod, annotationType, new Object[]{sourceId, fieldName}, queryParams);
    }
}
