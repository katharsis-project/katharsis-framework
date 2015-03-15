package io.katharsis.context;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class SampleJsonApplicationContextTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidClassShouldReturnInstance() {
        // GIVEN
        SampleJsonApplicationContext sut = new SampleJsonApplicationContext();

        // WHEN
        Object object = sut.getInstance(Object.class);

        // THEN
        Assert.assertNotNull(object);
    }

    @Test
    public void onClassWithPrivateConstructorShouldThrowException() {
        // GIVEN
        SampleJsonApplicationContext sut = new SampleJsonApplicationContext();

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.getInstance(Arrays.class);
    }
}
