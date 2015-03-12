package io.katharsis.path;

public class PathBuilder {

    public ResourcePath<?> buildPath(String path) {
        String[] strings = path.split("/");

        ResourcePath previousPath = null;

        for (int i = 1; i <= strings.length / 2; i += 2) {
            previousPath = new ResourcePath<>(strings[i], new PathIds<>(strings[i + 1]));
        }


        return previousPath;
    }
}
