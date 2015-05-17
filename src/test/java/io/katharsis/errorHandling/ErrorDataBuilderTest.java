package io.katharsis.errorHandling;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorDataBuilderTest {

    private static final String DETAIL = "detail";
    private static final String CODE = "code";
    private static final String HREF = "href";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String TITLE = "title";
    private static final List<String> LINKS = Arrays.asList("link1", "link2");
    private static final List<String> PATHS = Arrays.asList("path1", "path2");

    @Test
    public void shouldSetDetail() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setDetail(DETAIL)
                .build();
        assertThat(error.getDetail()).isEqualTo(DETAIL);
    }

    @Test
    public void shouldSetCode() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setCode(CODE)
                .build();
        assertThat(error.getCode()).isEqualTo(CODE);
    }

    @Test
    public void shouldSetHref() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setHref(HREF)
                .build();
        assertThat(error.getHref()).isEqualTo(HREF);
    }

    @Test
    public void shouldSetId() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setId(ID)
                .build();
        assertThat(error.getId()).isEqualTo(ID);
    }

    @Test
    public void shouldSetStatus() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setStatus(STATUS)
                .build();
        assertThat(error.getStatus()).isEqualTo(STATUS);
    }

    @Test
    public void shouldSetTitle() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setTitle(TITLE)
                .build();
        assertThat(error.getTitle()).isEqualTo(TITLE);
    }

    @Test
    public void shouldSetLinks() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setLinks(LINKS)
                .build();
        assertThat(error.getLinks()).isEqualTo(LINKS);
    }

    @Test
    public void shouldSetPaths() throws Exception {
        ErrorData error = ErrorData.newBuilder()
                .setPaths(PATHS)
                .build();
        assertThat(error.getPaths()).isEqualTo(PATHS);
    }
}