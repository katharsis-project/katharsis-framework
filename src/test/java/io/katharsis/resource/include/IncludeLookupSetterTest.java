package io.katharsis.resource.include;

import io.katharsis.resource.registry.ResourceRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest {

    @Mock
    ResourceRegistry resourceRegistry;

    private IncludeLookupSetter sut;

    @Before
    public void setUp() throws Exception {
        sut= new IncludeLookupSetter(resourceRegistry);
    }

    @Test
    public void onInclusionSetterErrorShouldReturnError() throws Exception {
        // GIVEN

    }
}
