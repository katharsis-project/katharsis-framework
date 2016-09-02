package io.katharsis.resource.registry;


public class ConstantServiceUrlProvider implements ServiceUrlProvider {

    private String result = null;

    public ConstantServiceUrlProvider(String result) {
        this.result = result;
    }

    @Override
    public String getUrl() {
        return result;
    }
}
