package io.katharsis.utils;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassUtilsTest {

    @Test
    public void onClassInheritanceShouldReturnInheritedClasses() throws Exception {
        // WHEN
        List<Field> result = ClassUtils.getClassFields(ChildClass.class);

        // THEN
        assertThat(result).hasSize(2);
    }

    @Test
    public void onGetGettersShouldReturnMethodsStartingWithGet() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("setParentField", String.class));
    }

    @Test
    public void onGetGettersShouldReturnMethodsThatNotTakeParams() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("getParentFieldWithParameter", String.class));
    }


    @Test
    public void onGetGettersShouldReturnMethodsThatReturnValue() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("getParentFieldReturningVoid"));
    }

    @Test
    public void onClassInheritanceShouldReturnInheritedGetters() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ChildClass.class);

        // THEN
        assertThat(result).hasSize(2);
    }


    public static class ParentClass {

        private String parentField;

        public String getParentField() {
            return parentField;
        }

        public void setParentField(String parentField) {
            this.parentField = parentField;
        }

        public String getParentFieldWithParameter(String parameter) {
            return parentField;
        }

        public void getParentFieldReturningVoid() {
        }
    }

    public static class ChildClass extends ParentClass {
        private String childField;

        public String getChildField() {
            return childField;
        }

    }
}
