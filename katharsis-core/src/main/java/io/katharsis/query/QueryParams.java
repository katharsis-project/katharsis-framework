package io.katharsis.query;

import io.katharsis.errorhandling.exception.QueryParseException;
import io.katharsis.queryParams.PaginationKey;
import io.katharsis.utils.java.Optional;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class QueryParams {

    Map<String, FilterParam> filters = new HashMap<>();
    Map<String, GroupParam> grouping = new HashMap<>();

    Map<PaginationKey, PaginationParam> pagination = new HashMap<>();

    // Sparse fields grouped by type
    Map<String, FieldsParam> sparseFields = new HashMap<>();

    Optional<SortingParam> sorting = Optional.empty();
    Optional<IncludeParam> include = Optional.empty();

    Map<String, Set<String>> other = new HashMap<>();

    public QueryParams(@NonNull Map<String, Set<String>> params) {
        for (Map.Entry<String, Set<String>> param : params.entrySet()) {

            String lowerCasedParam = param.getKey().toLowerCase();

            if (lowerCasedParam.startsWith(FilterParam.PREFIX)) {

                FilterParam filter = FilterParam.build(param.getKey(), param.getValue());
                filters.put(filter.getQualifier(), filter);

            } else if (lowerCasedParam.startsWith(GroupParam.PREFIX)) {

                GroupParam group = GroupParam.build(param.getKey(), param.getValue());
                grouping.put(param.getKey(), group);

            } else if (lowerCasedParam.equalsIgnoreCase(SortingParam.PREFIX)) {
                sorting = Optional.of(SortingParam.build(param.getValue()));
            } else if (lowerCasedParam.startsWith(PaginationParam.PREFIX)) {

                PaginationParam paginationParam = PaginationParam.build(param.getKey(), param.getValue());
                pagination.put(paginationParam.getPaginationKey(), paginationParam);

            } else if (lowerCasedParam.startsWith(FieldsParam.PREFIX)) {
                FieldsParam fieldsParam = FieldsParam.build(param.getKey(), param.getValue());
                sparseFields.put(fieldsParam.getType(), fieldsParam);
            } else if (lowerCasedParam.startsWith(IncludeParam.PREFIX)) {
                if (include == null) {
                    throw new QueryParseException("Multiple values for 'include'" + param.getKey());
                }
                include = Optional.ofNullable(IncludeParam.build(param.getKey(), ""));
            } else {
                other.put(param.getKey(), param.getValue());
            }
        }
    }

    public FilterParam getFilter(@NonNull String qualifier) {
        return filters.get(qualifier);
    }

    public Optional<SortingParam> getSorting() {
        return sorting;
    }

    public GroupParam getGrouping(@NonNull String qualifier) {
        return grouping.get(qualifier);
    }

    public PaginationParam getPagination(@NonNull String qualifier) {
        return pagination.get(qualifier);
    }

    public Set<String> getOther(@NonNull String name) {
        return Collections.unmodifiableSet(other.get(name));
    }

}
