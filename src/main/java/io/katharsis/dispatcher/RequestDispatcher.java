package io.katharsis.dispatcher;

import io.katharsis.path.PathBuilder;
import io.katharsis.path.ResourcePath;

public class RequestDispatcher {

    private PathBuilder pathBuilder;

    public RequestDispatcher(PathBuilder pathBuilder) {
        this.pathBuilder = pathBuilder;
    }

    public void accept(String requestType, String path) {
        ResourcePath resourcePath = pathBuilder.buildPath(path);


    }
}
