package io.katharsis.errorhandling;

import java.util.List;

public class ErrorDataBuilder {
    private String id;
    private String href;
    private String status;
    private String code;
    private String title;
    private String detail;
    private List<String> links;
    private List<String> paths;

    /**
     * A unique identifier for this particular occurrence of the problem.
     */
    public ErrorDataBuilder setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * A URI that MAY yield further details about this particular occurrence of the problem.
     */
    public ErrorDataBuilder setHref(String href) {
        this.href = href;
        return this;
    }

    /**
     * The HTTP status code applicable to this problem, expressed as a string value.
     */
    public ErrorDataBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * An application-specific error code, expressed as a string value.
     */
    public ErrorDataBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * A short, human-readable summary of the problem.
     * It SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization.
     */
    public ErrorDataBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * A human-readable explanation specific to this occurrence of the problem.
     */
    public ErrorDataBuilder setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    /**
     * An array of JSON Pointers [RFC6901] to the associated resource(s) within the request document [e.g. ["/data"] for a primary data object].
     */
    public ErrorDataBuilder setLinks(List<String> links) {
        this.links = links;
        return this;
    }

    /**
     * An array of JSON Pointers to the relevant attribute(s) within the associated resource(s) in the request document.
     * Each path MUST be relative to the resource path(s) expressed in the error object's "links" member
     * [e.g. ["/first-name", "/last-name"] to reference a couple attributes].
     */
    public ErrorDataBuilder setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    public ErrorData build() {
        return new ErrorData(id, href, status, code, title, detail, links, paths);
    }
}