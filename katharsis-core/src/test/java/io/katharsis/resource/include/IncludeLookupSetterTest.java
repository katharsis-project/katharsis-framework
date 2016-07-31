package io.katharsis.resource.include;

import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.ClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest {

    @Mock
    ResourceRegistry resourceRegistry;

    private IncludeLookupSetter sut;

    @Before
    public void setUp() throws Exception {
        sut = new IncludeLookupSetter(resourceRegistry);
    }

    @Test
    public void onInclusionSetterErrorShouldReturnError() throws Exception {
        // GIVEN

    }

    @Test
    public void testGetClassFromFieldNameReturnsProperClass() throws Exception {
        Field field = ClassUtils.findClassField(TestClass.class, "simpleString");
        Class<?> clazz = sut.getClassFromField(field);

        assertThat(clazz.equals(String.class)).isTrue();

        field = ClassUtils.findClassField(TestClass.class, "localeCollection");
        clazz = sut.getClassFromField(field);

        assertThat(clazz.equals(Locale.class)).isTrue();

    }

    /**
     * https://github.com/katharsis-project/katharsis-core/issues/318
     *
     * @throws Exception
     */
    @Test
    public void testKebabCasedFieldIsResolved() throws Exception {

    }


    static class TestClass {
        private String simpleString;
        private List<Locale> localeCollection;
    }


}
