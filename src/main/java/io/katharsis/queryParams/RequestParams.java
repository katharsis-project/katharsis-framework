package io.katharsis.queryParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains a set of parameters passed along with the request.
 */
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
        SORTING_TYPE_REFERENCE = new TypeReference<Map<String, SortingValues>>() {};
        GROUPING_TYPE_REFERENCE = new TypeReference<List<String>>() {};
        PAGINATION_TYPE_REFERENCE = new TypeReference<Map<PaginationKeys, Integer>>() {};
        INCLUDED_FIELDS_TYPE_REFERENCE = new TypeReference<List<String>>() {};
        INCLUDED_RELATIONS_TYPE_REFERENCE = new TypeReference<List<String>>() {};
    }

    public RequestParams(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Contains a set of filters assigned to a request. <a href="http://jsonapi.org/format/#fetching-filtering">Filtering</a>
     *
     * @return set of filters sent along with the request
     */
    public JsonNode getFilters() {
        return filters != null ? filters.deepCopy() : null;
    }

    void setFilters(String filters) throws IOException {
        this.filters = objectMapper.readTree(filters);
    }

    /**
     * Contains a map of sorting values. <a href="http://jsonapi.org/format/#fetching-sorting">Sorting</a>
     * @return set of sorting fields assigned to a request
     */
    public Map<String, SortingValues> getSorting() {
        return sorting;
    }

    void setSorting(String sorting) throws IOException {
        this.sorting = Collections.unmodifiableMap(
                objectMapper.readValue(sorting, SORTING_TYPE_REFERENCE)
        );
    }

    public List getGrouping() {
        return grouping;
    }

    void setGrouping(String grouping) throws IOException {
        this.grouping = Collections.unmodifiableList(
                objectMapper.readValue(grouping, GROUPING_TYPE_REFERENCE)
        );
    }

    public Map<PaginationKeys, Integer> getPagination() {
        return pagination;
    }

    void setPagination(String pagination) throws IOException {
        this.pagination = Collections.unmodifiableMap(
                objectMapper.readValue(pagination, PAGINATION_TYPE_REFERENCE)
        );
    }

    public List getIncludedFields() {
        return includedFields;
    }

    void setIncludedFields(String includedFields) throws IOException {
        this.includedFields = Collections.unmodifiableList(
                objectMapper.readValue(includedFields, INCLUDED_FIELDS_TYPE_REFERENCE)
        );
    }

    /**
     * Get a set of included fields which should be included in the resource
     * @return included relationships
     */
    public List getIncludedRelations() {
        return includedRelations;
    }

    void setIncludedRelations(String includedRelations) throws IOException {
        this.includedRelations = Collections.unmodifiableList(
                objectMapper.readValue(includedRelations, INCLUDED_RELATIONS_TYPE_REFERENCE)
        );
    }

}
