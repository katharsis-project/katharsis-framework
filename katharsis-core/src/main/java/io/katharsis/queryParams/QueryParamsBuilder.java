package io.katharsis.queryParams;

import io.katharsis.errorhandling.exception.KatharsisException;
import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.utils.java.Optional;

import java.util.Map;
import java.util.Set;

/**
 * Builder responsible for building queryParams. The parameter parsing is being delegated to a parser implementation.
 * The created {@link QueryParams} object contains several fields where each of them is not-null only when
 * this parameter has been passed with a request.
 * <p>
 * ---------------------------------------------------------------------------------------------------------------------
 * POTENTIAL IMPROVEMENT NOTE : This can be made even more flexible by implementing the builder pattern to allow
 * provisioning of different parsers for each component as the QueryParamsBuilder is being built: I.e:
 * QueryParamsBuilder.builder().filters(myCustomFilterParser).sorting(myOtherCustomSortingParser)...build()
 * This way, the user can mix and match various parsing strategies for individual components.
 * QueryParamsParser could become a one method interface and this could be particularly useful to Java 8 users who
 * can simply pass instances of java.lang.Function to implement custom parsing per component (filter/sort/group/etc etc).
 */
public class QueryParamsBuilder {

    private final QueryParamsParser queryParamsParser;

    public QueryParamsBuilder(final QueryParamsParser queryParamsParser) {
        this.queryParamsParser = queryParamsParser;
    }

    /**
     * Decodes passed query parameters
     *
     * @param queryParams Map of provided query params
     * @return QueryParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public QueryParams buildQueryParams(Map<String, Set<String>> queryParams) {
        QueryParams deserializedQueryParams = new QueryParams();
        try {
            deserializedQueryParams.setFilters(this.queryParamsParser.parseFiltersParameters(queryParams));
            deserializedQueryParams.setSorting(this.queryParamsParser.parseSortingParameters(queryParams));
            deserializedQueryParams.setGrouping(this.queryParamsParser.parseGroupingParameters(queryParams));
            deserializedQueryParams.setPagination(this.queryParamsParser.parsePaginationParameters(queryParams));
            deserializedQueryParams.setIncludedFields(this.queryParamsParser.parseIncludedFieldsParameters(queryParams));
            deserializedQueryParams.setIncludedRelations(this.queryParamsParser.parseIncludedRelationsParameters(queryParams));
        } catch (KatharsisException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ParametersDeserializationException(e.getMessage());
        }
        return deserializedQueryParams;
    }

    /**
     * Decodes passed query parameters
     *
     * @return QueryParams containing filtered query params grouped by JSON:API standard
     * @throws ParametersDeserializationException thrown when unsupported input format is detected
     */
    public QueryParams parseQuery(Optional<String> query) throws KatharsisException {
        return buildQueryParams(DefaultQueryParamsParser.splitQuery(query.orElse("")));
    }

}
