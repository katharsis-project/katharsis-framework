package io.katharsis.jpa.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.core.internal.utils.PreconditionUtil;

public class QueryUtilTest {

  @Test
  public void testConstructorIsPrivate()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Constructor<PreconditionUtil> constructor = PreconditionUtil.class.getDeclaredConstructor();
    Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

}
