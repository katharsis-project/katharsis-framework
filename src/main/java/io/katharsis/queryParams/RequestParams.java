package io.katharsis.queryParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestParams {
    private JsonNode filters;
    private Map<String, SortingValues> sorting;
    private List<String> grouping;
    private Map<PaginationKeys, Integer> pagination;
    private List<String> includedFields;
    private List<String> includedRelations;

    private ObjectMapper objectMapper;

    private static final TypeReference SORTING_TYPE_REFERENCE;
    private static final TypeReference GROUPING_TYPE_REFERENCE;
    private static final TypeReference PAGINATION_TYPE_REFERENCE;
    private static final TypeReference INCLUDED_FIELDS_TYPE_REFERENCE;
    private static final TypeReference INCLUDED_RELATIONS_TYPE_REFERENCE;

    static {
        SORTING_TYPE_REFERENCE = new TypeReference<Map<String, SortingValues>>() {
        };
        GROUPING_TYPE_REFERENCE = new TypeReference<List<String>>() {
        };
        PAGINATION_TYPE_REFERENCE = new TypeReference<Map<PaginationKeys, Integer>>() {
        };
        INCLUDED_FIELDS_TYPE_REFERENCE = new TypeReference<List<String>>() {
        };
        INCLUDED_RELATIONS_TYPE_REFERENCE = new TypeReference<List<String>>() {
        };
    }

    public RequestParams(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode getFilters() {
        return filters;
    }

    public void setFilters(String filters) throws IOException {
        this.filters = objectMapper.readTree(filters);
    }

    public Map<String, SortingValues> getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) throws IOException {
        this.sorting = Collections.unmodifiableMap(
                objectMapper.readValue(sorting, SORTING_TYPE_REFERENCE)
        );
    }

    public List getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) throws IOException {
        this.grouping = Collections.unmodifiableList(
                objectMapper.readValue(grouping, GROUPING_TYPE_REFERENCE)
        );
    }

    public Map<PaginationKeys, Integer> getPagination() {
        return pagination;
    }

    public void setPagination(String pagination) throws IOException {
        this.pagination = Collections.unmodifiableMap(
                objectMapper.readValue(pagination, PAGINATION_TYPE_REFERENCE)
        );
    }

    public List getIncludedFields() {
        return includedFields;
    }

    public void setIncludedFields(String includedFields) throws IOException {
        this.includedFields = Collections.unmodifiableList(
                objectMapper.readValue(includedFields, INCLUDED_FIELDS_TYPE_REFERENCE)
        );
    }

    public List getIncludedRelations() {
        return includedRelations;
    }

    public void setIncludedRelations(String includedRelations) throws IOException {
        this.includedRelations = Collections.unmodifiableList(
                objectMapper.readValue(includedRelations, INCLUDED_RELATIONS_TYPE_REFERENCE)
        );
    }

}
