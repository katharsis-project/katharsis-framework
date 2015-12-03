package io.katharsis.resource.information;

import io.katharsis.resource.field.ResourceField;

public class ResourceFieldWrapper {
    private ResourceField resourceField;
    private boolean discarded;
    private String originalName;

    public ResourceFieldWrapper(ResourceField resourceField, boolean discarded, String originalName) {
        this.resourceField = resourceField;
        this.discarded = discarded;
        this.originalName = originalName;
    }

    public ResourceField getResourceField() {
        return resourceField;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public String getOriginalName() {
        return originalName;
    }
}
