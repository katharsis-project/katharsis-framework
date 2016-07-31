package io.katharsis.dispatcher;

import io.katharsis.domain.api.TopLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultResponseContext implements ResponseContext {

    private int httpStatus;
    private TopLevel document;

}
