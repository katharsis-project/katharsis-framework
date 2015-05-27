package io.katharsis.errorhandling;

import java.util.Arrays;
import java.util.List;

public class ErrorDataMother {

    public static final String DETAIL = "detail";
    public static final String CODE = "code";
    public static final String HREF = "href";
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String TITLE = "title";
    public static final List<String> LINKS = Arrays.asList("link1", "link2");
    public static final List<String> PATHS = Arrays.asList("path1", "path2");

    public static ErrorDataBuilder fullyPopulatedErrorDataBuilder() {
        return ErrorData.builder()
                .setDetail(DETAIL)
                .setStatus(STATUS)
                .setId(ID)
                .setCode(CODE)
                .setHref(HREF)
                .setTitle(TITLE)
                .setLinks(LINKS)
                .setPaths(PATHS);
    }

    public static ErrorData fullyPopulatedErrorData() {
        return fullyPopulatedErrorDataBuilder().build();
    }

    public static List<ErrorData> oneSizeCollectionOfErrorData() {
        return Arrays.asList(fullyPopulatedErrorData());
    }
}
