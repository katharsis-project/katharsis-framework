package io.katharsis.queryParams;

import java.util.List;

public class RequestParams {
    private List filters;
    private List sorting;
    private List grouping;
    private List pagination;
    private List includedFields;
    private List includedRelations;

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
