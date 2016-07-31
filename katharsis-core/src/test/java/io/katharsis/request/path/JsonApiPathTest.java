package io.katharsis.request.path;

import org.junit.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;


public class JsonApiPathTest {

    @Test
    public void testParseGetCollection() throws Exception {

        JsonApiPath uri = JsonApiPath.parsePath(URI.create("http://host.local/tasks").toURL());

        assertThat(uri.getResource()).isEqualToIgnoringCase("tasks");

        assertThat(uri.getIds().isPresent()).isEqualTo(false);
        assertThat(uri.getRelationship().isPresent()).isEqualTo(false);
        assertThat(uri.getField().isPresent()).isEqualTo(false);
    }

    @Test
    public void testParseGetCollectionWithMultipleIdsHasMultipleIds() throws Exception {
        JsonApiPath uri = JsonApiPath.parsePath(URI.create("http://host.local/tasks/1,2").toURL());

        assertThat(uri.getResource()).isEqualToIgnoringCase("tasks");
        assertThat(uri.getIds().isPresent()).isEqualTo(true);

        assertThat(uri.getIds().get().size()).isEqualTo(2);

        assertThat(uri.getRelationship().isPresent()).isEqualTo(false);
        assertThat(uri.getField().isPresent()).isEqualTo(false);
    }


    @Test
    public void testGetSingleResourceUrlHasOneId() throws Exception {
        JsonApiPath uri = JsonApiPath.parsePath(URI.create("http://host.local/tasks/1").toURL());

        assertThat(uri.getResource()).isEqualToIgnoringCase("tasks");
        assertThat(uri.getIds().isPresent()).isEqualTo(true);

        assertThat(uri.getIds().get().size()).isEqualTo(1);

        assertThat(uri.getRelationship().isPresent()).isEqualTo(false);
        assertThat(uri.getField().isPresent()).isEqualTo(false);
    }

    @Test
    public void testRelationshipUriHasRelationship() throws Exception {

        JsonApiPath uri = JsonApiPath.parsePath(URI.create("http://host.local/tasks/1/relationships/project").toURL());

        assertThat(uri.getResource()).isEqualToIgnoringCase("tasks");
        assertThat(uri.getIds().isPresent()).isEqualTo(true);

        assertThat(uri.getIds().get().size()).isEqualTo(1);

        assertThat(uri.getRelationship().isPresent()).isEqualTo(true);
        assertThat(uri.getRelationship().get()).isEqualTo("project");

        assertThat(uri.getField().isPresent()).isEqualTo(false);

    }

    @Test
    public void testJsonApiPathHasFieldForFieldUri() throws Exception {
        JsonApiPath uri = JsonApiPath.parsePath(URI.create("http://host.local/tasks/1/project").toURL());

        assertThat(uri.getResource()).isEqualToIgnoringCase("tasks");
        assertThat(uri.getIds().isPresent()).isEqualTo(true);

        assertThat(uri.getIds().get().size()).isEqualTo(1);

        assertThat(uri.getRelationship().isPresent()).isEqualTo(false);

        assertThat(uri.getField().isPresent()).isEqualTo(true);
        assertThat(uri.getField().get()).isEqualTo("project");
    }
}