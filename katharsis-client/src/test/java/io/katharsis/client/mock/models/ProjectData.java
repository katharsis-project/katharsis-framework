package io.katharsis.client.mock.models;

public class ProjectData {
    private String data;

    public String getData() {
        return data;
    }

    public ProjectData setData(@SuppressWarnings("SameParameterValue") String data) {
        this.data = data;
        return this;
    }
}
