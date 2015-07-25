package io.katharsis.errorhandling;

import java.util.HashMap;
import java.util.Map;

public class ErrorDataBuilder {
    private String id;
    private String aboutLink;
    private String status;
    private String code;
    private String title;
    private String detail;
    private String sourcePointer;
    private String sourceParameter;
    private Map<String, Object> meta;

    /**
     * @param id A unique identifier for this particular occurrence of the problem.
     * @return ErrorDataBuilder
     */
    public ErrorDataBuilder setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * A link that leads to further details about this particular occurrence of the problem.
     *
     * Wrapped in "links" object.
     */
    public ErrorDataBuilder setAboutLink(String aboutLink) {
        this.aboutLink = aboutLink;
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
     * A JSON Pointer [RFC6901] to the associated entity in the request document
     * [e.g. "/data" for a primary data object, or "/data/attributes/title" for a specific attribute].
     *
     * Wrapped in "source" object.
     *
     */
    public ErrorDataBuilder setSourcePointer(String sourcePointer) {
        this.sourcePointer = sourcePointer;
        return this;
    }

    /**
     * A string indicating which query parameter caused the error.
     *
     * Wrapped in "source" object.
     */
    public ErrorDataBuilder setSourceParameter(String sourceParameter) {
        this.sourceParameter = sourceParameter;
        return this;
    }

    /**
     * A meta object containing non-standard meta-information about the error.
     */
    public ErrorDataBuilder setMeta(Map<String, Object> meta) {
        this.meta = meta;
        return this;
    }

    public ErrorDataBuilder addMetaField(String key, Object value) {
        if (meta == null) {
            meta = new HashMap<>();
        }
        meta.put(key, value);
        return this;
    }

    public ErrorData build() {
        return new ErrorData(id, aboutLink, status, code, title, detail, sourcePointer, sourceParameter, meta);
    }
}