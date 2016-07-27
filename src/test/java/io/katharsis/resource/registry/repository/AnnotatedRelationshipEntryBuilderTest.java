package io.katharsis.resource.registry.repository;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class AnnotatedRelationshipEntryBuilderTest {

    @Test
    public void onInstanceOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), SimpleRelationshipRepository.class));


        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);
    }

    @Test
    public void onInstanceOfAnonymousDescendantOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new JsonServiceLocator() {
                @Override
                public <T> T getInstance(Class<T> clazz) {
                    return (T) new SimpleRelationshipRepository() {};
                }
            }, SimpleRelationshipRepository.class)
        );

        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);

    }

    @Test
    public void onInstanceOfNonAnnotatedClassShouldThrowIllegalArgumentException() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(
            new RepositoryInstanceBuilder(new JsonServiceLocator() {
                @Override
                public <T> T getInstance(Class<T> clazz) {
                    return (T) new Object();
                }
            }, SimpleRelationshipRepository.class)
        );

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
