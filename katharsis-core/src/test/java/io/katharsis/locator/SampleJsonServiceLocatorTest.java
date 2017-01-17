package io.katharsis.locator;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SampleJsonServiceLocatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidClassShouldReturnInstance() {
        // GIVEN
        SampleJsonServiceLocator sut = new SampleJsonServiceLocator();

        // WHEN
        Object object = sut.getInstance(Object.class);

        // THEN
        Assert.assertNotNull(object);
    }

    @Test
    public void onClassWithPrivateConstructorShouldThrowException() {
        // GIVEN
        SampleJsonServiceLocator sut = new SampleJsonServiceLocator();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getInstance(Arrays.class);
    }
}
