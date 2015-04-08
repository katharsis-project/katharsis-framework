package io.katharsis.queryParams;

import java.util.List;

public class RequestParams {
    public List filters;
    public List sorting;
    public List grouping;
    public List pagination;
    public List includedFields;
    public List includedRelations;

    public List getFilters() {
        return filters;
    }

    public void setFilters(List filters) {
        this.filters = filters;
    }

    public List getSorting() {
        return sorting;
    }

    public void setSorting(List sorting) {
        this.sorting = sorting;
    }

    public List getGrouping() {
        return grouping;
    }

    public void setGrouping(List grouping) {
        this.grouping = grouping;
    }

    public List getPagination() {
        return pagination;
    }

    public void setPagination(List pagination) {
        this.pagination = pagination;
    }

    public List getIncludedFields() {
        return includedFields;
    }

    public void setIncludedFields(List includedFields) {
        this.includedFields = includedFields;
    }

    public List getIncludedRelations() {
        return includedRelations;
    }

    public void setIncludedRelations(List includedRelations) {
        this.includedRelations = includedRelations;
    }

}
