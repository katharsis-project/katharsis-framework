package io.katharsis.queryParams.params;

import io.katharsis.queryParams.include.Inclusion;
import lombok.Value;

import java.util.Set;


@Value
public class IncludedRelationsParams {

    private Set<Inclusion> params;

}
