package io.katharsis.queryspec.internal;

import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;

public interface QueryAdapter {

    boolean hasIncludedRelations();

    TypedParams<IncludedRelationsParams> getIncludedRelations();

    TypedParams<IncludedFieldsParams> getIncludedFields();

}
