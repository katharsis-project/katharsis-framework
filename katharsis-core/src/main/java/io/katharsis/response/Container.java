package io.katharsis.response;

import io.katharsis.jackson.serializer.ContainerSerializer;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
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
    private final String includedFieldName;
    private final int includedIndex;
    private final List<String> pathList;
    private Set<String> additionalFields;

    public Container() {
        this.data = null;
        this.response = null;
        this.containerType = null;
        this.includedFieldName = null;
        this.includedIndex = -1;
        this.pathList = null;
    }

    public Container(Object data, BaseResponseContext response) {
        this.data = data;
        this.response = response;
        this.containerType = ContainerType.TOP;
        this.includedIndex = 0;
        this.includedFieldName = null;
        this.pathList = null;
    }

    public Container(Object data, BaseResponseContext response, ContainerType containerType) {
        this.data = data;
        this.response = response;
        this.containerType = containerType;
        if (containerType.equals(ContainerType.TOP)) {
            this.includedIndex = 0;
        } else {
            this.includedIndex = -1;
        }
        this.includedFieldName = null;
        this.pathList = null;

    }

    public Container(Object data, BaseResponseContext response, ContainerType containerType, String includedFieldName, int includedIndex, List<String> pathList) {
        this.data = data;
        this.response = response;
        this.includedFieldName = includedFieldName;
        this.containerType = containerType;
        this.includedIndex = includedIndex;
        this.pathList = pathList;
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

    public String getIncludedFieldName() {
        return includedFieldName;
    }

    public int getIncludedIndex() {
        return includedIndex;
    }

    public List<String> getPathList() {
        return pathList;
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

    public void appendAdditionalFields(String fieldName) {
        if (additionalFields == null) {
            this.additionalFields = new HashSet<>();
        }
        this.additionalFields.add(fieldName);
    }

    public Set<String> getAdditionalFields() {
        return additionalFields;
    }

}
