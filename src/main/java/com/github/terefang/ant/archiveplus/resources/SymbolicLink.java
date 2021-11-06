package com.github.terefang.ant.archiveplus.resources;

import org.apache.tools.ant.types.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SymbolicLink extends Resource
{
    private String name;
    private String target;

    private String user;
    private String group;
    private String permissions;

    private boolean linkTargetInContent;

    public boolean isLinkTargetInContent()
    {
        return linkTargetInContent;
    }

    public void setLinkTargetInContent(boolean linkTargetInContent) {
        this.linkTargetInContent = linkTargetInContent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "SymbolicLink{" +
                "name='" + name + '\'' +
                ", target='" + target + '\'' +
                ", user='" + user + '\'' +
                ", group='" + group + '\'' +
                ", permissions='" + permissions + '\'' +
                '}';
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        if(this.linkTargetInContent)
        {
            return new ByteArrayInputStream(this.getTarget().getBytes());
        }
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }
}
