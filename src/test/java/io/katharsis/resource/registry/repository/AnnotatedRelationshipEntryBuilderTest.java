package io.katharsis.resource.registry.repository;

import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AnnotatedRelationshipEntryBuilderTest {

    @Test
    public void onInstanceOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final SimpleRelationshipRepository repositoryInstance = new SimpleRelationshipRepository();
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(repositoryInstance);


        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);
    }

    @Test
    public void onInstanceOfAnonymousDescendantOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final Object repositoryInstance = new SimpleRelationshipRepository() {};
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(repositoryInstance);

        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);

    }

    @Test
    public void onInstanceOfNonAnnotatedClassShouldThrowIllegalArgumentException() {

        // GIVEN
        final Object repositoryInstance = new Object();
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(repositoryInstance);

        // WHEN
        try {
            builder.getTargetAffiliation();
        } catch (Exception e) {
            // THEN
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @JsonApiRelationshipRepository(source = String.class, target = Character.class)
    public static class SimpleRelationshipRepository {

    }

}
