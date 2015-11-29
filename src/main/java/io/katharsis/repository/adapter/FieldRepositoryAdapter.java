package io.katharsis.repository.adapter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.annotations.JsonApiAddField;
import io.katharsis.repository.annotations.JsonApiAddFields;
import io.katharsis.repository.annotations.JsonApiDeleteField;
import io.katharsis.repository.annotations.JsonApiDeleteFields;
import io.katharsis.utils.ClassUtils;

import java.lang.reflect.Method;

public class FieldRepositoryAdapter<T, T_ID, D, D_ID>
    extends RepositoryAdapter<T>
    implements FieldRepository<T, T_ID, D, D_ID> {

    private Method addFieldMethod;
    private Method addFieldsMethod;
    private Method deleteFieldMethod;
    private Method deleteFieldsMethod;

    public FieldRepositoryAdapter(Object implementationObject, ParametersFactory parametersFactory) {
        super(implementationObject, parametersFactory);
    }

    @Override
    public D addField(T_ID resource, D field, String fieldName, QueryParams queryParams) {
        Class<JsonApiAddField> annotationType = JsonApiAddField.class;
        if (addFieldMethod == null) {
            addFieldMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Object[] firstParameters = {resource, field, fieldName};
        return invokeOperation(addFieldMethod, annotationType, firstParameters, queryParams);
    }

    @Override
    public Iterable<D> addFields(T_ID resource, Iterable<D> fields, String fieldName, QueryParams queryParams) {
        Class<JsonApiAddFields> annotationType = JsonApiAddFields.class;
        if (addFieldsMethod == null) {
            addFieldsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Object[] firstParameters = {resource, fields, fieldName};
        return invokeOperation(addFieldsMethod, annotationType, firstParameters, queryParams);
    }

    @Override
    public void deleteField(T_ID resource, String fieldName, QueryParams queryParams) {
        Class<JsonApiDeleteField> annotationType = JsonApiDeleteField.class;
        if (deleteFieldMethod == null) {
            deleteFieldMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Object[] firstParameters = {resource, fieldName};
        invokeOperation(deleteFieldMethod, annotationType, firstParameters, queryParams);
    }

    @Override
    public void deleteFields(T_ID resource, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParams) {
        Class<JsonApiDeleteFields> annotationType = JsonApiDeleteFields.class;
        if (deleteFieldsMethod == null) {
            deleteFieldsMethod = ClassUtils.findMethodWith(implementationObject, annotationType);
        }
        Object[] firstParameters = {resource, targetIds, fieldName};
        invokeOperation(deleteFieldsMethod, annotationType, firstParameters, queryParams);
    }
}
