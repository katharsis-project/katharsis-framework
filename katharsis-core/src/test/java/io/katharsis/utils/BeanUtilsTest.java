package io.katharsis.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanUtilsTest {

	// TODO seems wrong
//    @Test
//    public void onNullValueShouldReturnNull() throws Exception {
//        // GIVEN
//        PropertyUtilsTest.Bean bean = new PropertyUtilsTest.Bean();
//
//        // WHEN
//        Object result = BeanUtils.getProperty(bean, "publicProperty");
//
//        // THEN
//        assertThat(result).isEqualTo("null");
//    }

    @Test
    public void onNonNullNullValueShouldReturnStringifiedProperty() throws Exception {
        // GIVEN
        PropertyUtilsTest.Bean bean = new PropertyUtilsTest.Bean();
        bean.setBooleanPrimitivePropertyWithMutators(true);

        // WHEN
        Object result = BeanUtils.getProperty(bean, "booleanPrimitivePropertyWithMutators");

        // THEN
        assertThat(result).isEqualTo("true");
    }
}
