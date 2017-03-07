package io.katharsis.legacy.queryParams;

import java.util.Map;

import io.katharsis.core.internal.utils.CompareUtils;
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
    public int hashCode() {
        int result = filters != null ? filters.hashCode() : 0;
        result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
        result = 31 * result + (grouping != null ? grouping.hashCode() : 0);
        result = 31 * result + (includedFields != null ? includedFields.hashCode() : 0);
        result = 31 * result + (includedRelations != null ? includedRelations.hashCode() : 0);
        result = 31 * result + (pagination != null ? pagination.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        QueryParams other = (QueryParams) obj;
        return CompareUtils.isEquals(filters, other.filters)
                && CompareUtils.isEquals(sorting, other.sorting)
                && CompareUtils.isEquals(grouping, other.grouping)
                && CompareUtils.isEquals(includedFields, other.includedFields)
                && CompareUtils.isEquals(includedRelations, other.includedRelations)
                && CompareUtils.isEquals(pagination, other.pagination);
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
