package io.katharsis.resource.mock.models;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "memoranda")
public class Memorandum extends Document {
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
