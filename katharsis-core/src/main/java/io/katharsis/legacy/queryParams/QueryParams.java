package io.katharsis.legacy.queryParams;

import java.util.Map;

import io.katharsis.legacy.queryParams.params.*;

/**
 * Contains a set of parameters passed along with the request.
 * 
 * @deprecated make use of QuerySpec
 */
@Deprecated
public class QueryParams {
    private TypedParams<FilterParams> filters;
    private TypedParams<SortingParams> sorting;
    private TypedParams<GroupingParams> grouping;
    private TypedParams<IncludedFieldsParams> includedFields;
    private TypedParams<IncludedRelationsParams> includedRelations;
    private Map<RestrictedPaginationKeys, Integer> pagination;

    public TypedParams<FilterParams> getFilters() {
        return filters;
    }
    void setFilters(TypedParams<FilterParams> filters) {
        this.filters = filters;
    }

    public TypedParams<SortingParams> getSorting() {
        return sorting;
    }
    void setSorting(TypedParams<SortingParams> sorting) {
        this.sorting = sorting;
    }

    public TypedParams<GroupingParams> getGrouping() {
        return grouping;
    }
    void setGrouping(TypedParams<GroupingParams> grouping) {
        this.grouping = grouping;
    }

    public Map<RestrictedPaginationKeys, Integer> getPagination() {
        return pagination;
    }
    void setPagination(Map<RestrictedPaginationKeys, Integer> pagination) {
        this.pagination = pagination;
    }

    public TypedParams<IncludedFieldsParams> getIncludedFields() {
        return includedFields;
    }
    void setIncludedFields(TypedParams<IncludedFieldsParams> includedFields) {
        this.includedFields = includedFields;
    }

    public TypedParams<IncludedRelationsParams> getIncludedRelations() {
        return includedRelations;
    }
    void setIncludedRelations(TypedParams<IncludedRelationsParams> includedRelations) {
        this.includedRelations = includedRelations;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
            "filters=" + filters +
            ", sorting=" + sorting +
            ", grouping=" + grouping +
            ", includedFields=" + includedFields +
            ", includedRelations=" + includedRelations +
            ", pagination=" + pagination +
            '}';
    }
}
