package io.katharsis.resource.field;

import java.lang.reflect.Type;
import java.util.Objects;

public class ResourceField {
	
	public enum LookupIncludeBehavior{
		NONE,
		AUTOMATICALLY_WHEN_NULL,
		AUTOMATICALLY_ALWAYS,
	}
	
    private final String jsonName;
    private final String underlyingName;
    private final Class<?> type;
    private final Type genericType;
    private final boolean lazy;
	private LookupIncludeBehavior lookupIncludeBehavior;
	private boolean includeByDefault;

	public ResourceField(String jsonName, String underlyingName, Class<?> type, Type genericType) {
    	this(jsonName, underlyingName, type, genericType, true, false, LookupIncludeBehavior.NONE);
    }
    
    public ResourceField(String jsonName, String underlyingName, Class<?> type, Type genericType, boolean lazy, boolean includeByDefault, LookupIncludeBehavior lookupIncludeBehavior) {
        this.jsonName = jsonName;
        this.underlyingName = underlyingName;
        this.includeByDefault = includeByDefault;
        this.type = type;
        this.genericType = genericType;
        this.lazy = lazy;
        this.lookupIncludeBehavior = lookupIncludeBehavior;
    }
    
    /**
     * See also {@link JsonApiLookupIncludeAutomatically}}
     */
    public LookupIncludeBehavior getLookupIncludeAutomatically(){
    	return lookupIncludeBehavior;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getUnderlyingName() {
        return underlyingName;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    /**
     * Returns a flag which indicate if a field should not be serialized automatically.
     * 
     * @return true if a field is lazy
     */
    public boolean isLazy() {
    	return lazy;
    }

	public boolean getIncludeByDefault() {
		return includeByDefault;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResourceField that = (ResourceField) o;
        return Objects.equals(jsonName, that.jsonName) &&
            Objects.equals(underlyingName, that.underlyingName) &&
            Objects.equals(type, that.type) &&
            Objects.equals(lookupIncludeBehavior, that.lookupIncludeBehavior) &&
            Objects.equals(includeByDefault, that.includeByDefault) &&
            Objects.equals(genericType, that.genericType) &&
            Objects.equals(lazy, that.lazy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonName, underlyingName, type, genericType, lazy, includeByDefault, lookupIncludeBehavior);
    }
}