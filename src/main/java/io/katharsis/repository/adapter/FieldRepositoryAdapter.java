package io.katharsis.repository.adapter;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.FieldRepository;
import io.katharsis.repository.ParametersFactory;
import io.katharsis.repository.annotations.JsonApiAddField;
import io.katharsis.utils.ClassUtils;

import java.lang.reflect.Method;

public class FieldRepositoryAdapter<T, T_ID, D, D_ID>
    extends RepositoryAdapter<T>
    implements FieldRepository<T, T_ID, D, D_ID> {

    private Method addFieldMethod;
    private Method updateFieldMethod;
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
        checkIfNotNull(annotationType, addFieldMethod);

        Object[] methodParameters = parametersFactory
            .buildParameters(new Object[]{resource, field, fieldName}, addFieldMethod, queryParams, annotationType);

        return invoke(addFieldMethod, methodParameters);
    }

    @Override
    public D updateField(T_ID resource, D field, String fieldName, QueryParams queryParam) {
        return null;
    }

    @Override
    public D deleteField(T_ID resource, String fieldName, QueryParams queryParam) {
        return null;
    }

    @Override
    public Iterable<D> deleteFields(T_ID resource, Iterable<D_ID> targetIds, String fieldName, QueryParams queryParam) {
        return null;
    }
}
