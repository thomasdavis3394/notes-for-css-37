package org.dromara.easytl.runtime;

public class UrlComponents {
    public String scheme;
    public String userInfo;
    public String host;
    public Integer port;
    public String path;
    public String query;
    public String ref;

    public UrlComponents() {}

    public String getScheme() { return scheme; }
    public String getUserInfo() { return userInfo; }
    public String getHost() { return host; }
    public Integer getPort() { return port; }
    public String getPath() { return path; }
    public String getQuery() { return query; }
    public String getRef() { return ref; }
}