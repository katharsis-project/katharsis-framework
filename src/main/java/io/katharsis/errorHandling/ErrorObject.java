package io.katharsis.errorHandling;

import java.util.List;
import java.util.Objects;

public class ErrorObject {

    /**
     * A unique identifier for this particular occurrence of the problem.
     */
    private final String id;

    /**
     * A URI that MAY yield further details about this particular occurrence of the problem.
     */
    private final String href;

    /**
     * The HTTP status code applicable to this problem, expressed as a string value.
     */
    private final String status;

    /**
     * An application-specific error code, expressed as a string value.
     */
    private final String code;

    /**
     * A short, human-readable summary of the problem.
     * It SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization.
     */
    private final String title;

    /**
     * A human-readable explanation specific to this occurrence of the problem.
     */
    private final String detail;

    /**
     * An array of JSON Pointers [RFC6901] to the associated resource(s) within the request document [e.g. ["/data"] for a primary data object].
     */
    private final List<String> links;

    /**
     * An array of JSON Pointers to the relevant attribute(s) within the associated resource(s) in the request document.
     * Each path MUST be relative to the resource path(s) expressed in the error object's "links" member
     * [e.g. ["/first-name", "/last-name"] to reference a couple attributes].
     */
    private final List<String> paths;

    public ErrorObject(String id, String href, String status, String code, String title, String detail, List<String> links, List<String> paths) {
        this.id = id;
        this.href = href;
        this.status = status;
        this.code = code;
        this.title = title;
        this.detail = detail;
        this.links = links;
        this.paths = paths;
    }

    public static ErrorObjectBuilder newBuilder() {
        return new ErrorObjectBuilder();
    }

    public String getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public List<String> getLinks() {
        return links;
    }

    public List<String> getPaths() {
        return paths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorObject)) return false;
        ErrorObject that = (ErrorObject) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(href, that.href) &&
                Objects.equals(status, that.status) &&
                Objects.equals(code, that.code) &&
                Objects.equals(title, that.title) &&
                Objects.equals(detail, that.detail) &&
                Objects.equals(links, that.links) &&
                Objects.equals(paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, href, status, code, title, detail, links, paths);
    }

    @Override
    public String toString() {
        return "ErrorObject{" +
                "id='" + id + '\'' +
                ", href='" + href + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", links=" + links +
                ", paths=" + paths +
                '}';
    }
}
