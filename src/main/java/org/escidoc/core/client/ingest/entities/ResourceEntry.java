package org.escidoc.core.client.ingest.entities;

public class ResourceEntry {
    private String title;

    private String identifier;

    private String objectType;

    // private ResourceType resourceType;
    private String href;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    // public ResourceType getResourceType() {
    // return resourceType;
    // }
    //
    // public void setResourceType(ResourceType resourceType) {
    // this.resourceType = resourceType;
    // }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        String s = getTitle() + " (";
        if (getObjectType() != null) {
            s += getObjectType() + " ";
        }
        s += getIdentifier() + ")";
        return s;
    }
}
