package com.github.terefang.ant.archiveplus.resources;

import org.apache.tools.ant.types.Resource;

public class ConfigFileResource extends Resource
{
    String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
