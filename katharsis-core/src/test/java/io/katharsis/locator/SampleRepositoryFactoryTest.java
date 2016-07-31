package io.katharsis.locator;

import io.katharsis.dispatcher.registry.annotated.ParametersFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class SampleRepositoryFactoryTest {

    ParametersFactory parametersFactory = new ParametersFactory();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidClassShouldReturnInstance() {
        // GIVEN
        NewInstanceRepositoryFactory sut = new NewInstanceRepositoryFactory(parametersFactory);

        // WHEN
        Object object = sut.getInstance(Object.class);

        // THEN
        Assert.assertNotNull(object);
    }

    @Test
    public void onClassWithPrivateConstructorShouldThrowException() {
        // GIVEN
        NewInstanceRepositoryFactory sut = new NewInstanceRepositoryFactory(parametersFactory);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getInstance(Arrays.class);
    }
}
