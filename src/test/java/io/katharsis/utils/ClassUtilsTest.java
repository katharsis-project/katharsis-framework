package io.katharsis.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.utils.java.Optional;
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
    public void onClassInheritanceShouldReturnInheritedField() throws Exception {
        // WHEN
        Field result = ClassUtils.findClassField(ChildClass.class, "parentField");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("parentField");
        assertThat(result.getDeclaringClass()).isEqualTo(ParentClass.class);
    }

    @Test
    public void onGetGettersShouldReturnMethodsStartingWithGet() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("aetParentField"));
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
    public void onGetGettersShouldReturnBooleanGettersThatHaveName() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("is"));
    }

    @Test
    public void onGetGettersShouldReturnNonBooleanGettersThatHaveName() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ParentClass.class);

        // THEN
        assertThat(result).doesNotContain(ParentClass.class.getDeclaredMethod("get"));
    }

    @Test
    public void onClassInheritanceShouldReturnInheritedGetters() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassGetters(ChildClass.class);

        // THEN
        assertThat(result).hasSize(4);
    }

    @Test
    public void onGetSettersShouldReturnMethodsThatSetValue() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassSetters(ParentClass.class);

        // THEN
        assertThat(result).containsOnly(ParentClass.class.getDeclaredMethod("setValue", String.class));
    }

    @Test
    public void onClassInheritanceShouldReturnInheritedSetters() throws Exception {
        // WHEN
        List<Method> result = ClassUtils.getClassSetters(ChildClass.class);

        // THEN
        assertThat(result).hasSize(1);
    }

    @Test
    public void onGetJsonApiResourceClassReturnCorrectClass() {
        // WHEN
        Class<? super ResourceClass$Proxy> clazz = ClassUtils.getJsonApiResourceClass(ResourceClass$Proxy.class);

        // THEN
        assertThat(clazz).isNotNull();
        assertThat(clazz).hasAnnotation(JsonApiResource.class);
        assertThat(clazz).isEqualTo(ResourceClass.class);
    }

    @Test
    public void onGetJsonApiResourceClassReturnCorrectInstanceClass() {
        ResourceClass$Proxy resource = new ResourceClass$Proxy();

        // WHEN
        Class<? super ResourceClass$Proxy> clazz = ClassUtils.getJsonApiResourceClass(resource);

        // THEN
        assertThat(clazz).isEqualTo(ResourceClass.class);
    }

    @Test
    public void onGetJsonApiResourceClassReturnNoInstanceClass() {
        ParentClass resource = new ParentClass();

        // WHEN
        Class<? super ParentClass> clazz = ClassUtils.getJsonApiResourceClass(resource);

        // THEN
        assertThat(clazz).isNull();
    }

    @Test
    public void onGetAnnotationShouldReturnAnnotation() {
        // WHEN
        Optional<JsonApiResource> result = ClassUtils.getAnnotation(ResourceClass.class, JsonApiResource.class);

        // THEN
        assertThat(result.get()).isInstanceOf(JsonApiResource.class);
    }

    @Test
    public void onGetAnnotationShouldReturnParentAnnotation() {
        // WHEN
        Optional<JsonPropertyOrder> result = ClassUtils.getAnnotation(ChildClass.class, JsonPropertyOrder.class);

        // THEN
        assertThat(result.get()).isInstanceOf(JsonPropertyOrder.class);
    }

    @Test
    public void onNonExistingAnnotationShouldReturnEmptyResult() {
        // WHEN
        Optional<JsonIgnore> result = ClassUtils.getAnnotation(ResourceClass.class, JsonIgnore.class);

        // THEN
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void onValidClassShouldCreateNewInstance() throws Exception {
        // WHEN
        ResourceClass result = ClassUtils.newInstance(ResourceClass.class);

        // THEN
        assertThat(result).isInstanceOf(ResourceClass.class);
    }

    @Test(expected = IllegalStateException.class)
    public void onClassWithCrushingConstructorShouldThrowException() throws Exception {
        // WHEN
        ClassUtils.newInstance(ClassWithCrashingConstructor.class);
    }

    @Test(expected = ResourceException.class)
    public void onClassWithoutDefaultConstructorShouldThrowException() throws Exception {
        // WHEN
        ClassUtils.newInstance(ClassWithoutDefaultConstructor.class);
    }

    @JsonPropertyOrder(alphabetic = true)
    public static class ParentClass {

        private String parentField;

        public String getParentField() {
            return parentField;
        }

        public String aetParentField() {
            return parentField;
        }

        public String getParentFieldWithParameter(String parameter) {
            return parentField;
        }

        public void getParentFieldReturningVoid() {
        }

        public void setValue(String value) {

        }

        public void setValueWithoutParameter() {

        }

        public boolean isPrimitiveBooleanProperty() {
            return true;
        }

        public Boolean isBooleanProperty() {
            return true;
        }

        public boolean is() {
            return true;
        }

        public String get() {
            return "value";
        }

        public void set(String value) {
        }
    }

    public static class ChildClass extends ParentClass {
        private String childField;

        public String getChildField() {
            return childField;
        }
    }

    @JsonApiResource(type = "resource")
    public static class ResourceClass {

    }

    public static class ClassWithCrashingConstructor {
        public ClassWithCrashingConstructor() {
            throw new IllegalStateException();
        }
    }

    public static class ClassWithoutDefaultConstructor {
        public ClassWithoutDefaultConstructor(String value) {
        }
    }

    public static class ResourceClass$Proxy extends ResourceClass{

    }
}
