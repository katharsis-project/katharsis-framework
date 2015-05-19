package io.katharsis.errorhandling;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorDataBuilderTest {

    @Test
    public void shouldSetDetail() throws Exception {
        ErrorData error = ErrorData.builder()
                .setDetail(ErrorDataMother.DETAIL)
                .build();
        assertThat(error.getDetail()).isEqualTo(ErrorDataMother.DETAIL);
    }

    @Test
    public void shouldSetCode() throws Exception {
        ErrorData error = ErrorData.builder()
                .setCode(ErrorDataMother.CODE)
                .build();
        assertThat(error.getCode()).isEqualTo(ErrorDataMother.CODE);
    }

    @Test
    public void shouldSetHref() throws Exception {
        ErrorData error = ErrorData.builder()
                .setHref(ErrorDataMother.HREF)
                .build();
        assertThat(error.getHref()).isEqualTo(ErrorDataMother.HREF);
    }

    @Test
    public void shouldSetId() throws Exception {
        ErrorData error = ErrorData.builder()
                .setId(ErrorDataMother.ID)
                .build();
        assertThat(error.getId()).isEqualTo(ErrorDataMother.ID);
    }

    @Test
    public void shouldSetStatus() throws Exception {
        ErrorData error = ErrorData.builder()
                .setStatus(ErrorDataMother.STATUS)
                .build();
        assertThat(error.getStatus()).isEqualTo(ErrorDataMother.STATUS);
    }

    @Test
    public void shouldSetTitle() throws Exception {
        ErrorData error = ErrorData.builder()
                .setTitle(ErrorDataMother.TITLE)
                .build();
        assertThat(error.getTitle()).isEqualTo(ErrorDataMother.TITLE);
    }

    @Test
    public void shouldSetLinks() throws Exception {
        ErrorData error = ErrorData.builder()
                .setLinks(ErrorDataMother.LINKS)
                .build();
        assertThat(error.getLinks()).isEqualTo(ErrorDataMother.LINKS);
    }

    @Test
    public void shouldSetPaths() throws Exception {
        ErrorData error = ErrorData.builder()
                .setPaths(ErrorDataMother.PATHS)
                .build();
        assertThat(error.getPaths()).isEqualTo(ErrorDataMother.PATHS);
    }
}