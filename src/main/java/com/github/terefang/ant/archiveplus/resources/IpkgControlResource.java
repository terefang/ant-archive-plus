package com.github.terefang.ant.archiveplus.resources;

import org.apache.tools.ant.types.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class IpkgControlResource extends Resource
{
    String packageName;
    String priority;
    String depends;
    String section;
    String description;
    String maintainer;
    String source;
    String version;
    String architecture;
    Properties additionalFields = new Properties();

    StringBuilder _sb;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public Properties getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(Properties additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void addAdditionalField(String _key, String _value)
    {
        this.additionalFields.setProperty(_key, _value);
    }

    @Override
    public long getSize()
    {
        checkBuilder();
        return this._sb.toString().getBytes().length;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream(this._sb.toString().getBytes());
    }

    private void checkBuilder() {
        if(_sb==null)
        {
            _sb = new StringBuilder();
            _sb.append("Package: "+this.getPackageName()+"\n");
            if(this.getProject()!=null)
            {
                _sb.append("Priority: ");
                _sb.append(this.getPriority());
                _sb.append("\n");
            }
            if(this.getDepends()!=null)
            {
                _sb.append("Depends: ");
                _sb.append(this.getDepends());
                _sb.append("\n");
            }
            if(this.getSection()!=null)
            {
                _sb.append("Section: ");
                _sb.append(this.getSection());
                _sb.append("\n");
            }
            if(this.getMaintainer()!=null)
            {
                _sb.append("Maintainer: ");
                _sb.append(this.getMaintainer());
                _sb.append("\n");
            }
            if(this.getSource()!=null)
            {
                _sb.append("Source: ");
                _sb.append(this.getSource());
                _sb.append("\n");
            }
            _sb.append("Version: "+this.getVersion()+"\n");
            _sb.append("Architecture: "+this.getArchitecture()+"\n");
            _sb.append("Description: "+this.getDescription()+"\n");
            _sb.append("X-Created-On: "+(new Date().toLocaleString())+"\n");
            if(additionalFields.keySet().size()>0)
            {
                for(String _key : additionalFields.stringPropertyNames())
                {
                    _sb.append(_key);
                    _sb.append(": ");
                    _sb.append(additionalFields.getProperty(_key));
                    _sb.append("\n");
                }
            }
            _sb.append("\n");
        }
    }
}
