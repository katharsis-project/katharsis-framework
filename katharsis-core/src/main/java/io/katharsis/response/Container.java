package io.katharsis.response;

import io.katharsis.jackson.serializer.ContainerSerializer;
import lombok.ToString;

import java.util.Objects;

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

    public Container(Object data, BaseResponseContext response) {
        this.data = data;
        this.response = response;
        this.containerType = ContainerType.TOP;
        this.includedIndex = 0;
        topResourceType = null;
    }

    public Container(Object data, BaseResponseContext response, ContainerType containerType, int level, String topResourceType) {
        this.data = data;
        this.response = response;
        this.containerType = containerType;
        this.includedIndex = level;
        this.topResourceType = topResourceType;
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
