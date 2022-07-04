package com.github.terefang.ant.archiveplus.resources;

import org.apache.tools.ant.types.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EmptyDirectory extends Resource
{
    private String name;

    private String user;
    private String group;
    private String dirmode;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

    public String getDirmode() {
        return dirmode;
    }

    public void setDirmode(String dirmode) {
        this.dirmode = dirmode;
    }

    @Override
    public String toString() {
        return "EmptyDirectory{" +
                "name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", group='" + group + '\'' +
                ", dirmode='" + dirmode + '\'' +
                '}';
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

}
