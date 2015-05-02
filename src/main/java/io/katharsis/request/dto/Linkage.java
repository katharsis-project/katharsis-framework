package io.katharsis.request.dto;

public class Linkage {
    private String type;
    private String id;

    public Linkage() {
    }

    public Linkage(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
