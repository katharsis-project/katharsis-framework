package io.katharsis.errorHandling;

import java.util.List;

public class ErrorObjectBuilder {
    private String id;
    private String href;
    private String status;
    private String code;
    private String title;
    private String detail;
    private List<String> links;
    private List<String> paths;

    public ErrorObjectBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ErrorObjectBuilder setHref(String href) {
        this.href = href;
        return this;
    }

    public ErrorObjectBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public ErrorObjectBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    public ErrorObjectBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ErrorObjectBuilder setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public ErrorObjectBuilder setLinks(List<String> links) {
        this.links = links;
        return this;
    }

    public ErrorObjectBuilder setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    public ErrorObject build() {
        return new ErrorObject(id, href, status, code, title, detail, links, paths);
    }
}