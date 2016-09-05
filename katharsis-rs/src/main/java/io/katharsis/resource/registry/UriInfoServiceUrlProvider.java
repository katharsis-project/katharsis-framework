package io.katharsis.resource.registry;

import javax.ws.rs.core.UriInfo;


public class UriInfoServiceUrlProvider implements ServiceUrlProvider {

    private UriInfo info;

    public UriInfoServiceUrlProvider(UriInfo info) {
        this.info = info;
    }

    @Override
    public String getUrl() {
        return info.getBaseUri().toString();
    }
}
