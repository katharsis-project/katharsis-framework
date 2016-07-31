package io.katharsis.query;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class FieldsParamTest {

    @Test
    public void testParseFieldsParamReturnsTheType() throws Exception {
        FieldsParam param = FieldsParam.build("fields[test]", new HashSet<String>());
        assertThat(param.getType()).isEqualTo("test");
    }

    @Test
    public void testParseFieldsParamSplitsSingleFieldName() throws Exception {
        FieldsParam param = FieldsParam.build("fields[test]", new HashSet<>(Collections.singleton("author")));
        assertThat(param.getType()).isEqualTo("test");
        assertThat(param.getFieldNames().size()).isEqualTo(1);

        assertThat(param.getFieldNames().get(0)).isEqualToIgnoringCase("author");
    }

    @Test
    public void testParseFieldsParamSplitsMultiValuedFieldName() throws Exception {
        FieldsParam param = FieldsParam.build("fields[test]", new HashSet<>(Collections.singleton("author,comments,blah,bih-bah")));
        assertThat(param.getType()).isEqualTo("test");
        assertThat(param.getFieldNames().size()).isEqualTo(4);

        assertThat(param.getFieldNames().get(1)).isEqualToIgnoringCase("comments");
        assertThat(param.getFieldNames().get(3)).isEqualToIgnoringCase("bih-bah");
    }


    @Test
    public void testParseFieldsParamSplitsMultiValuedFieldName_multipleQueryParams() throws Exception {
        // query like: tasks?fields[test]=author,comments&fields[test]=blah,bih-bah
        List<String> queryParams = new ArrayList<>();
        queryParams.add("author,comments");
        queryParams.add("blah,bih-bah");

        FieldsParam param = FieldsParam.build("fields[test]", queryParams);
        assertThat(param.getType()).isEqualTo("test");
        assertThat(param.getFieldNames().size()).isEqualTo(4);

        assertThat(param.getFieldNames().get(1)).isEqualToIgnoringCase("comments");
        assertThat(param.getFieldNames().get(3)).isEqualToIgnoringCase("bih-bah");
    }
}