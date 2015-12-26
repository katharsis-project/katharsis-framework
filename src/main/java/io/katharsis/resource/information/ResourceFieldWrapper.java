package io.katharsis.resource.information;

import io.katharsis.resource.field.ResourceField;

public class ResourceFieldWrapper {
    private ResourceField resourceField;
    private boolean discarded;

    public ResourceFieldWrapper(ResourceField resourceField, boolean discarded) {
        this.resourceField = resourceField;
        this.discarded = discarded;
    }

    public ResourceField getResourceField() {
        return resourceField;
    }

    public boolean isDiscarded() {
        return discarded;
    }
}
