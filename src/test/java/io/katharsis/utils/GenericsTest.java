package io.katharsis.utils;

import org.junit.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericsTest {

    @Test
    public void onClassWithStringParameterShouldBuildNewInstance() throws Exception {
        // WHEN
        Serializable serializable = Generics.castIdValue("1", ObjectId.class);

        // THEN
        assertThat(serializable).isExactlyInstanceOf(ObjectId.class);
        assertThat(((ObjectId) serializable).getId()).isEqualTo("1");
    }

    public static class ObjectId implements Serializable {
        private String id;

        public ObjectId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
