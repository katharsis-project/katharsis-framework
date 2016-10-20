package io.katharsis.queryspec;

import io.katharsis.jackson.exception.ParametersDeserializationException;
import io.katharsis.queryParams.RestrictedPaginationKeys;

/**
 * Default deserializer for the pagination types supported by {@link RestrictedPaginationKeys}
 * <p>
 * It favors limit/offset params over page number/size.
 * This means if a limit/offset param is encountered, the resulting {@link PagingSpec} will be offset/limit based,
 * regardless if a pageNumber/Size is also encountered.
 * <p>
 * Mixing offset/limit and number/size isn't something that makes sense anyways.
 */
public class DefaultPageParamDeserializer {

    private static final String OFFSET_PARAMETER = RestrictedPaginationKeys.offset.name();

    private static final String LIMIT_PARAMETER = RestrictedPaginationKeys.limit.name();

    private static final String NUMBER_PARAMETER = RestrictedPaginationKeys.number.name();

    private static final String SIZE_PARAMETER = RestrictedPaginationKeys.size.name();

    private Long pageNumber = null;
    private Long pageSize = null;
    private Long limit = null;
    private Long offset = null;

    /**
     * Parse a single parameter
     *
     * @param parameter
     */
    public void collectPageParam(DefaultQuerySpecDeserializer.Parameter parameter) {
        if (!parameter.name.startsWith("[") || !parameter.name.endsWith("]")) {
            throw new ParametersDeserializationException(parameter.toString());
        }
        String name = parameter.name.substring(1, parameter.name.length() - 1);
        if (OFFSET_PARAMETER.equalsIgnoreCase(name)) {
            offset = parameter.getLongValue();
        } else if (LIMIT_PARAMETER.equalsIgnoreCase(name)) {
            limit = parameter.getLongValue();
        } else if (SIZE_PARAMETER.equalsIgnoreCase(name)) {
            pageSize = parameter.getLongValue();
        } else if (NUMBER_PARAMETER.equalsIgnoreCase(name)) {
            pageNumber = parameter.getLongValue();
        } else {
            throw new ParametersDeserializationException(parameter.toString());
        }
    }

    /**
     * Deserializes the collected parameters into an appropriate {@link PagingSpec}
     *
     * @return the PagingSpec represented by the parsed parameters
     */
    public PagingSpec deserialize() {
        PagingSpec pagingSpec = null;
        if (limit != null) {
            if (offset == null)
                offset = 0L;
            pagingSpec = new OffsetBasedPagingSpec(offset, limit, OFFSET_PARAMETER, LIMIT_PARAMETER);
        }
        if (pagingSpec == null && pageSize != null) {
            if (pageNumber == null)
                pageNumber = 0L;
            pagingSpec = new PageBasedPagingSpec(pageNumber, pageSize, NUMBER_PARAMETER, SIZE_PARAMETER);
        }
        return pagingSpec;
    }
}
