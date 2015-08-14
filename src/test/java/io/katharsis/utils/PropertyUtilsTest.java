package io.katharsis.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onNullBeanGetShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.getProperty(null, Bean.class.getDeclaredField("privatePropertyWithMutators"));
    }

    @Test
    public void onNullFieldGetShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.getProperty(bean, null);
    }

    @Test
    public void onBooleanPrimitiveWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.setBooleanPrimitivePropertyWithMutators(true);

        // WHEN
        Object result = PropertyUtils
            .getProperty(bean, Bean.class.getDeclaredField("booleanPrimitivePropertyWithMutators"));

        // THEN
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void onBooleanWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.setBooleanPropertyWithMutators(true);

        // WHEN
        Object result = PropertyUtils.getProperty(bean, Bean.class.getDeclaredField("booleanPropertyWithMutators"));

        // THEN
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void onStringPublicWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();
        bean.publicProperty = "value";

        // WHEN
        Object result = PropertyUtils.getProperty(bean, Bean.class.getDeclaredField("publicProperty"));

        // THEN
        assertThat(result).isEqualTo("value");
    }

    @Test
    public void onStringProtectedGetWithMutatorsShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        PropertyUtils.getProperty(bean, Bean.class.getDeclaredField("protectedProperty"));
    }

    @Test
    public void onInheritedStringPrivateWithMutatorsShouldReturnValue() throws Exception {
        // GIVEN
        Bean bean = new ChildBean();
        bean.setPrivatePropertyWithMutators("value");

        // WHEN
        Object result = PropertyUtils.getProperty(bean, Bean.class.getDeclaredField("privatePropertyWithMutators"));

        // THEN
        assertThat(result).isEqualTo("value");
    }

    @Test
    public void onNullBeanSetShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.setProperty(null, Bean.class.getDeclaredField("privatePropertyWithMutators"), null);
    }

    @Test
    public void onNullFieldSetShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        PropertyUtils.setProperty(bean, null, null);
    }

    @Test
    public void onBooleanPrimitiveWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, Bean.class.getDeclaredField("booleanPrimitivePropertyWithMutators"), true);

        // THEN
        assertThat(bean.isBooleanPrimitivePropertyWithMutators()).isEqualTo(true);
    }

    @Test
    public void onBooleanWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, Bean.class.getDeclaredField("booleanPropertyWithMutators"), true);

        // THEN
        assertThat(bean.getBooleanPropertyWithMutators()).isEqualTo(true);
    }

    @Test
    public void onStringPublicWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // WHEN
        PropertyUtils.setProperty(bean, Bean.class.getDeclaredField("publicProperty"), "value");

        // THEN
        assertThat(bean.publicProperty).isEqualTo("value");
    }

    @Test
    public void onStringProtectedSetWithMutatorsShouldThrowException() throws Exception {
        // GIVEN
        Bean bean = new Bean();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        PropertyUtils.setProperty(bean, Bean.class.getDeclaredField("protectedProperty"), null);
    }

    @Test
    public void onInheritedStringPrivateWithMutatorsShouldSetValue() throws Exception {
        // GIVEN
        Bean bean = new ChildBean();

        // WHEN
        PropertyUtils.setProperty(bean, Bean.class.getDeclaredField("privatePropertyWithMutators"), "value");

        // THEN
        assertThat(bean.getPrivatePropertyWithMutators()).isEqualTo("value");
    }

    public static class Bean {
        private String privatePropertyWithMutators;
        private boolean booleanPrimitivePropertyWithMutators;
        private Boolean booleanPropertyWithMutators;
        public String publicProperty;
        protected String protectedProperty;

        public String getPrivatePropertyWithMutators() {
            return privatePropertyWithMutators;
        }

        public void setPrivatePropertyWithMutators(String privatePropertyWithMutators) {
            this.privatePropertyWithMutators = privatePropertyWithMutators;
        }

        public boolean isBooleanPrimitivePropertyWithMutators() {
            return booleanPrimitivePropertyWithMutators;
        }

        public void setBooleanPrimitivePropertyWithMutators(boolean booleanPrimitivePropertyWithMutators) {
            this.booleanPrimitivePropertyWithMutators = booleanPrimitivePropertyWithMutators;
        }

        public Boolean getBooleanPropertyWithMutators() {
            return booleanPropertyWithMutators;
        }

        public void setBooleanPropertyWithMutators(Boolean booleanPropertyWithMutators) {
            this.booleanPropertyWithMutators = booleanPropertyWithMutators;
        }
    }

    public static class ChildBean extends Bean {

    }
}
