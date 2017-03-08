package io.katharsis.resource.mock.models;

import java.io.Serializable;

public interface Identifiable<T extends Serializable & Comparable<T>> {
    T getId();
}
