package io.katharsis.legacy.internal;

import java.io.Serializable;
import java.lang.reflect.Method;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.legacy.repository.annotations.JsonApiAddRelations;
import io.katharsis.legacy.repository.annotations.JsonApiFindManyTargets;
import io.katharsis.legacy.repository.annotations.JsonApiFindOneTarget;
import io.katharsis.legacy.repository.annotations.JsonApiRemoveRelations;
import io.katharsis.legacy.repository.annotations.JsonApiSetRelation;
import io.katharsis.legacy.repository.annotations.JsonApiSetRelations;
import io.katharsis.repository.request.QueryAdapter;

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

    public void setRelation(T source, D_ID targetId, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiSetRelation> annotationType = JsonApiSetRelation.class;
        if (setRelationMethod == null) {
            setRelationMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(setRelationMethod, annotationType, new Object[]{source, targetId, fieldName}, queryAdapter);
    }

    public void setRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiSetRelations> annotationType = JsonApiSetRelations.class;
        if (setRelationsMethod == null) {
            setRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(setRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryAdapter);
    }

    public void addRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiAddRelations> annotationType = JsonApiAddRelations.class;
        if (addRelationsMethod == null) {
            addRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(addRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryAdapter);
    }

    public void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiRemoveRelations> annotationType = JsonApiRemoveRelations.class;
        if (removeRelationsMethod == null) {
            removeRelationsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        invokeOperation(removeRelationsMethod, annotationType, new Object[]{source, targetIds, fieldName}, queryAdapter);
    }

    public Object findOneTarget(T_ID sourceId, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiFindOneTarget> annotationType = JsonApiFindOneTarget.class;
        if (findOneTargetMethod == null) {
            findOneTargetMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findOneTargetMethod, annotationType, new Object[]{sourceId, fieldName}, queryAdapter);
    }

    public Object findManyTargets(T_ID sourceId, String fieldName, QueryAdapter queryAdapter) {
        Class<JsonApiFindManyTargets> annotationType = JsonApiFindManyTargets.class;
        if (findManyTargetsMethod == null) {
            findManyTargetsMethod = ClassUtils.findMethodWith(implementationClass, annotationType);
        }
        return invokeOperation(findManyTargetsMethod, annotationType, new Object[]{sourceId, fieldName}, queryAdapter);
    }
}
