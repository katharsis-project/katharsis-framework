package io.katharsis.response.builder;

import javassist.*;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class builds a response object that is then passed to Jackson. It takes original resource and basing on that
 * creates a new proxy class on-the-fly.
 */
public class ResponseBuilder {

    private static final String DATA_FIELD_NAME = "data";

    /**
     * Atomic long is used to assure that there is no the same name within the same class loader. Long value should
     * be suitable.
     */
    private static final AtomicLong idCounter = new AtomicLong();

    public Object buildResponse(Object resource, Class<?> resourceType, boolean isCollection)
            throws CannotCompileException, NotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        ClassPool pool = createPool();
        String className = getClassName(resourceType);
        CtClass TopLevelJson = pool.makeClass(className);

        CtField dataField = createDataField(resource, resourceType, isCollection, pool, TopLevelJson);
        TopLevelJson.addField(dataField);
        Class aClass = TopLevelJson.toClass();

        Object newInstance = aClass.newInstance();
        if (isCollection && (resource == null || !((Iterable) resource).iterator().hasNext())) {
            aClass.getDeclaredField(DATA_FIELD_NAME).set(newInstance, Collections.emptyList());
        }
        return newInstance;
    }

    private CtField createDataField(Object resource, Class<?> resourceType, boolean isCollection, ClassPool pool, CtClass topLevelJson) throws NotFoundException, CannotCompileException {
        CtField dataField;
        if (isCollection) {
            if (resource != null && !Iterable.class.isAssignableFrom(resource.getClass())) {
                throw new RuntimeException("Resource is not iterable");
            }
            CtClass iterableCtClass = pool.getCtClass(Iterable.class.getCanonicalName());
            dataField = new CtField(iterableCtClass, DATA_FIELD_NAME, topLevelJson);
        } else {
            CtClass resourceCtClass = pool.getCtClass(resourceType.getCanonicalName());
            dataField = new CtField(resourceCtClass, DATA_FIELD_NAME, topLevelJson);
        }
        dataField.setModifiers(Modifier.PUBLIC);
        return dataField;
    }

    private ClassPool createPool() {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(this.getClass()));
        return pool;
    }

    private String getClassName(Class<?> resourceType) {
        StringBuilder sb = new StringBuilder("io.katharsis.resource.proxy$");
        sb.append(resourceType.getCanonicalName())
                .append("$")
                .append(idCounter.getAndIncrement());
        return sb.toString();
    }
}
