package io.katharsis.response;

import io.katharsis.jackson.serializer.ContainerSerializer;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class responsible for representing a single data filed within top-level JSON object returned by Katharsis. The
 * resulting JSON is serialized using {@link ContainerSerializer}.
 */
@ToString
public class Container {

    private final Object data;
    private final BaseResponseContext response;
    private final ContainerType containerType;
    private final int includedIndex;
    private final String topResourceType;
    private Set<Integer> additionalIndexes;

    public Container(Object data, BaseResponseContext response) {
        this.data = data;
        this.response = response;
        this.containerType = ContainerType.TOP;
        this.includedIndex = 0;
        topResourceType = null;
        additionalIndexes = null;
    }

    public Container(Object data, BaseResponseContext response, ContainerType containerType, int level, String topResourceType) {
        this.data = data;
        this.response = response;
        this.containerType = containerType;
        this.includedIndex = level;
        this.topResourceType = topResourceType;
        additionalIndexes = new HashSet<>();
    }

    public BaseResponseContext getResponse() {
        return response;
    }


    public Object getData() {
        return data;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public int getIncludedIndex() {
        return includedIndex;
    }

    public String getTopResourceType() {
        return topResourceType;
    }

    public Set<Integer> getAdditionalIndexes() {
        return additionalIndexes;
    }

    public void appendIncludedIndex(int includedIndex) {
        additionalIndexes.add(includedIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Container container = (Container) o;
        return Objects.equals(data, container.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

}
