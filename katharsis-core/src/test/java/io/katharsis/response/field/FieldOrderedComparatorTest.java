package io.katharsis.response.field;

import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.field.FieldOrderedComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldOrderedComparatorTest {

    ResourceField fieldA;
    ResourceField fieldB;

    @Before
    public void setUp() throws Exception {
        fieldA = new ResourceField("a", "a", String.class, String.class);
        fieldB = new ResourceField("b", "b", String.class, String.class);
    }

    @Test
    public void onTwoFieldsShouldSortCorrectly() throws Exception {
        // GIVEN
        Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{
            "b", "a"
        }, false));

        // WHEN
        fields.add(fieldA);
        fields.add(fieldB);

        // THEN
        assertThat(fields).containsSequence(fieldB, fieldA);
    }

    @Test
    public void onOneFieldShouldSortCorrectly() throws Exception {
        // GIVEN
        Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{
            "b"
        }, false));

        // WHEN
        fields.add(fieldA);
        fields.add(fieldB);

        // THEN
        assertThat(fields).containsSequence(fieldB, fieldA);
    }

    @Test
    public void onNoOrderShouldPersistInsertionOrder() throws Exception {
        // GIVEN
        Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, false));

        // WHEN
        fields.add(fieldB);
        fields.add(fieldA);

        // THEN
        assertThat(fields).containsSequence(fieldB, fieldA);
    }

    @Test
    public void onAlphabeticOrderShouldSortCorrectly() throws Exception {
        // GIVEN
        Set<ResourceField> fields = new TreeSet<>(new FieldOrderedComparator(new String[]{}, true));

        // WHEN
        fields.add(fieldB);
        fields.add(fieldA);

        // THEN
        assertThat(fields).containsSequence(fieldA, fieldB);
    }
}
