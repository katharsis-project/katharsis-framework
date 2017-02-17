package io.katharsis.resource.registry.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.katharsis.legacy.registry.RepositoryInstanceBuilder;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.module.ModuleRegistry;

@SuppressWarnings("unchecked")
public class AnnotatedRelationshipEntryBuilderTest {
	
	private ModuleRegistry moduleRegistry = new ModuleRegistry();
	

    @Test
    public void onInstanceOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(moduleRegistry,
            new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), SimpleRelationshipRepository.class));


        // WHEN
        final Class<?> targetClass = builder.getTargetAffiliation();

        // THEN
        assertThat(targetClass).isEqualTo(Character.class);
    }

    @Test
    public void onInstanceOfAnonymousDescendantOfAnnotatedRelationshipRepositoryShouldReturnTargetClass() {

        // GIVEN
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(moduleRegistry,
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
        final AnnotatedRelationshipEntryBuilder builder = new AnnotatedRelationshipEntryBuilder(moduleRegistry,
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
