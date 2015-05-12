package io.katharsis.response.builder;

import javassist.ClassPool;
import javassist.CtClass;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class builds a response object that is then passed to Jackson. It takes original resource and basing on that
 * creates a new proxy class on-the-fly.
 */
public class ResponseBuilder {

    /**
     * Atomic long is used to assure that there is no the same name within the same class loader. Long value should
     * be suitable.
     */
    private static final AtomicLong idCounter = new AtomicLong();

    public Object buildResponse(Object resource) {
        ClassPool pool = ClassPool.getDefault();
        String className = getClassName(resource);
        CtClass cc = pool.makeClass(className);

        return null;
    }

    private String getClassName(Object resource) {
        StringBuilder sb = new StringBuilder("io.katharsis.resource.proxy$");
        sb.append(resource.getClass())
                .append("$")
                .append(idCounter.getAndIncrement());
        return sb.toString();
    }
}
