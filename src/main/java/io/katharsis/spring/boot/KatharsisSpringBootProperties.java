package io.katharsis.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("katharsis")
public class KatharsisSpringBootProperties {
    private String resourcePackage;
    private String domainName;
    private String pathPrefix;

    public String getResourcePackage() {
        return resourcePackage;
    }

    public void setResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}
