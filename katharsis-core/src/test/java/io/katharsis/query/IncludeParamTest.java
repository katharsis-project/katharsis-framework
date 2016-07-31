package io.katharsis.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class IncludeParamTest {

    @Test
    public void testIncludeFieldWIthResourceIsParsedCorrectly() throws Exception {
        IncludeParam param = IncludeParam.build("include", "simple");

        assertThat(param.getPaths().size()).isEqualTo(1);
        assertThat(param.getPaths().get(0)).isEqualTo("simple");
    }

    @Test
    public void testIncludeFieldWIthResourcePath() throws Exception {
        IncludeParam param = IncludeParam.build("include", "comments.author");

        assertThat(param.getPaths().size()).isEqualTo(1);
        assertThat(param.getPaths().get(0)).isEqualTo("comments.author");
    }

    @Test
    public void testIncludeFieldWIthListOfInclude() throws Exception {
        IncludeParam param = IncludeParam.build("include", "author,comments.author, sample");

        assertThat(param.getPaths().size()).isEqualTo(3);
        assertThat(param.getPaths().get(1)).isEqualTo("comments.author");
        assertThat(param.getPaths().get(2)).isEqualTo(" sample");
    }
    

}