package com.github.terefang.ant.archiveplus.taskdefs;

import com.github.terefang.ant.archiveplus.ArchiveEnum;
import com.github.terefang.ant.archiveplus.CompressionEnum;
import com.github.terefang.ant.archiveplus.resources.*;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.TarFileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class IpkgTask extends Task
{
    boolean verbose;
    File tempdir = new File("/tmp");
    Resource dest;

    CompressionEnum compression = CompressionEnum.gzip;
    ArchiveEnum format = ArchiveEnum.ar;

    // -- control.tar.gz
    ResourceCollection preinstall;
    ResourceCollection preremove;
    ResourceCollection postinstall;
    ResourceCollection postremove;
    ResourceCollection control;
    List<ConfigFileResource> conffiles = new Vector<>();

    // -- data.tar.gz
    List<ResourceCollection> sources = new Vector();

    @Override
    @SneakyThrows
    public void execute() {
        //Path _path = Files.createTempDirectory(IpkgTask.class.getSimpleName());
        // NOTE -- opkg 0.4.5/0.5.0 does use tar/ar and variants
        if(ArchiveEnum.ar.equals(this.format)
                || ArchiveEnum.bsdar.equals(this.format)
                || ArchiveEnum.pax.equals(this.format)
                || ArchiveEnum.tar.equals(this.format))
        {
            this.log("creating ipk " + this.dest.getName()+" ("+this.format.name()+"-format)");
        }
        else
        {
            String _msg = "invalid pkg format given: " + this.format.name();
            this.log(_msg);
            throw new IllegalArgumentException(_msg);
        }

        ArchiveOutputStream _out = ArchiveEnum.createStream(this.format, this.dest.getOutputStream());
        // -- create temp debian-binary -> "2.0"
        ArchiveEntry _ent = ArchiveEnum.createEntry(this.format, "debian-binary", false, false, 3, 0, 0, 0444, 0);
        _out.putArchiveEntry(_ent);
        _out.write("2.0".getBytes());
        _out.closeArchiveEntry();
        _out.flush();

        ByteArrayOutputStream _baos = new ByteArrayOutputStream();

        // -- create temp data.tar.gz
        this.log("creating ipk/data ..."+(this.sources.size()>0 ? "YES" : "NONE"));
        ArchiveOutputStream _data = ArchiveEnum.createStream(ArchiveEnum.pax, CompressionEnum.createStream(this.compression, _baos));
        // NOTE -- opkg 0.4.5 can support .xz .bz2 .lz4
        // NOTE -- opkg 0.5.0 adds .zst
        // NOTE -- i dont know how to properly support .lz4
        for(ResourceCollection _rc : this.sources)
        {
            checkAndWriteEntries(_data, "ipk/data", _rc);
        }
        _data.flush();
        _data.close();

        // -- then ar or tar or tgz the ipkg in that order
        _ent = ArchiveEnum.createEntry(this.format,"data.tar"+CompressionEnum.createSuffix(this.compression), false, false, _baos.toByteArray().length, 0, 0, 0644, 0);
        _out.putArchiveEntry(_ent);
        _out.write(_baos.toByteArray());
        _out.closeArchiveEntry();
        _out.flush();


        // -- create temp control.tar.gz
        _baos = new ByteArrayOutputStream();
        ArchiveOutputStream _control = ArchiveEnum.createStream(ArchiveEnum.pax, CompressionEnum.createStream(this.compression, _baos));
        checkAndWriteEntry(_control, "ipk", "control", this.control);
        if(this.control==null) throw new IllegalArgumentException("missing control file!");
        checkAndWriteEntry(_control, "ipk/control", "preinst", this.preinstall);
        checkAndWriteEntry(_control, "ipk/control", "postinst", this.postinstall);
        checkAndWriteEntry(_control, "ipk/control", "prerm", this.preremove);
        checkAndWriteEntry(_control, "ipk/control", "postrm", this.postremove);
        checkAndWriteConfFiles(_control, "ipk/control", "conffiles", this.conffiles);
        _control.flush();
        _control.close();

        // -- then ar or tar or tgz the ipkg in that order
        _ent = ArchiveEnum.createEntry(this.format, "control.tar"+CompressionEnum.createSuffix(this.compression), false, false, _baos.toByteArray().length, 0, 0, 0644, 0);
        _out.putArchiveEntry(_ent);
        _out.write(_baos.toByteArray());
        _out.closeArchiveEntry();
        _out.flush();

        _out.close();
    }

    @SneakyThrows
    private void checkAndWriteConfFiles(ArchiveOutputStream _control, String _prefix, String _name, List<ConfigFileResource> _conffiles)
    {
        if(_conffiles==null) return;
        if(_conffiles.size()<1) return;

        StringBuilder _sb = new StringBuilder();
        for(ConfigFileResource _cf : _conffiles)
        {
            _sb.append(convertConfigFileResourceToPath(_cf)+"\n");
        }

        checkAndWriteEntry(_control, _prefix, _name, _sb);
    }

    @SneakyThrows
    private void checkAndWriteEntries(ArchiveOutputStream _archive, String _prefix, ResourceCollection _rc)
    {
        TarArchiveEntry _ent;
        Iterator<Resource> _it = _rc.iterator();
        while(_it.hasNext())
        {
            Resource _r = _it.next();
            if(_r instanceof EmptyDirectory)
            {
                EmptyDirectory _edir = (EmptyDirectory)_r;
                _ent = new TarArchiveEntry(_edir.getName(), TarConstants.LF_DIR, false);
                _ent.setGroupName(_edir.getGroup()==null ? "nobody" : _edir.getGroup());
                _ent.setUserName(_edir.getUser()==null ? "nobody" : _edir.getUser());
                _ent.setMode(Integer.parseInt(_edir.getDirmode()==null ? "400" : _edir.getDirmode(), 8));
                _archive.putArchiveEntry(_ent);
                _archive.closeArchiveEntry();
                _archive.flush();
                if(this.verbose) this.log("wrote "+_prefix+" "+_edir.getName()+" (emptypath)");
            }
            else
            if(_r instanceof SymbolicLink)
            {
                SymbolicLink _sl = (SymbolicLink)_r;
                _sl.setLinkTargetInContent(false);
                _ent = new TarArchiveEntry(_sl.getName(), TarConstants.LF_SYMLINK, false);
                _ent.setGroupName(_sl.getGroup()==null ? "nobody" : _sl.getGroup());
                _ent.setUserName(_sl.getUser()==null ? "nobody" : _sl.getUser());
                _ent.setLinkName(_sl.getTarget());
                _ent.setMode(Integer.parseInt(_sl.getPermissions()==null ? "400" : _sl.getPermissions(), 8));
                _archive.putArchiveEntry(_ent);
                _archive.closeArchiveEntry();
                _archive.flush();
                if(this.verbose) this.log("wrote "+_prefix+" "+_sl.getName()+" (symlink)");
            }
            else
            if(_r instanceof FileResource && _rc instanceof TarFileSet)
            {
                TarFileSet _tfs = (TarFileSet)_rc;
                FileResource _fr = (FileResource)_r;

                boolean isDir = _fr.isDirectory();
                String name = _fr.getName();

                if(StringUtils.isNotBlank(_tfs.getPrefix()))
                {
                    if(_tfs.getPrefix().endsWith("/"))
                    {
                        name = _tfs.getPrefix()+name;
                    }
                    else
                    {
                        name = _tfs.getPrefix()+"/"+name;
                    }
                }

                if(StringUtils.isNotBlank(_tfs.getFullpath()) && _rc.size()==1)
                {
                    name = _tfs.getFullpath();
                }

                if (isDir && !name.endsWith("/")) {
                    name += "/";
                } else if (!isDir && name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }

                _ent = new TarArchiveEntry(name, false);

                _ent.setModTime(_fr.getLastModified());
                _ent.setSize(isDir ? 0 : _fr.getSize());

                if (!isDir && _tfs.hasFileModeBeenSet()) {
                    _ent.setMode(_tfs.getFileMode());
                } else if (isDir && _tfs.hasDirModeBeenSet()) {
                    _ent.setMode(_tfs.getDirMode());
                } else if (_tfs.hasFileModeBeenSet()) {
                    _ent.setMode(_tfs.getFileMode());
                } else {
                    _ent.setMode(isDir
                            ? ArchiveFileSet.DEFAULT_DIR_MODE
                            : ArchiveFileSet.DEFAULT_FILE_MODE);
                }

                if (_tfs.hasUserIdBeenSet()) {
                    _ent.setUserId(_tfs.getUid());
                }

                if (_tfs.hasGroupIdBeenSet()) {
                    _ent.setGroupId(_tfs.getGid());
                }

                if (_tfs.hasUserNameBeenSet()) {
                    _ent.setUserName(_tfs.getUserName());
                }

                if (_tfs.hasGroupBeenSet()) {
                    _ent.setGroupName(_tfs.getGroup());
                }

                _archive.putArchiveEntry(_ent);
                if(!isDir)
                {
                    try (InputStream _in = _fr.getInputStream()) {
                        IOUtils.copy(_in, _archive);
                    }
                }
                _archive.closeArchiveEntry();
                _archive.flush();
                if(this.verbose) this.log("wrote "+_prefix+" "+name);
            }
            else
            {
                if(this.verbose) this.log("skipping unknown resource: "+_r.getClass().getCanonicalName());
            }
        }
    }

    @SneakyThrows
    private void checkAndWriteEntry(ArchiveOutputStream _archive, String _prefix, String _name, ResourceCollection _rc)
    {
        this.log("creating "+_prefix+" "+_name+" ... " + (_rc != null ? "YES" : "NONE"));
        if (_rc != null)
        {
            Resource _r = _rc.iterator().next();
            TarArchiveEntry _ent = new TarArchiveEntry(_name, false);
            _ent.setMode(0555);
            _ent.setSize(_r.getSize());
            _archive.putArchiveEntry(_ent);
            try (InputStream _in = _r.getInputStream()) {
                IOUtils.copy(_in, _archive);
            }
            _archive.closeArchiveEntry();
            _archive.flush();
        }
    }

    @SneakyThrows
    private void checkAndWriteEntry(ArchiveOutputStream _archive, String _prefix, String _name, StringBuilder _sb)
    {
        this.log("creating "+_prefix+" "+_name+" ... " + (_sb != null ? "YES" : "NONE"));
        if (_sb != null)
        {
            TarArchiveEntry _ent = new TarArchiveEntry(_name, false);
            _ent.setMode(0555);
            _ent.setSize(_sb.toString().getBytes(StandardCharsets.UTF_8).length);
            _archive.putArchiveEntry(_ent);
            IOUtil.copy(_sb.toString(), _archive);
            _archive.closeArchiveEntry();
            _archive.flush();
        }
    }

    @SneakyThrows
    public String convertConfigFileResourceToPath(ConfigFileResource _rc)
    {
        return ((ConfigFileResource)_rc).getName();
    }

    public void add(ResourceCollection _rc)
    {

        if(_rc instanceof ConfigFileResource)
        {
            this.conffiles.add((ConfigFileResource)_rc);
        }
        else
        if(_rc instanceof IpkgControlResource)
        {
            this.setControl(_rc);
        }
        else
        if(_rc instanceof IpkgPreInstallResource)
        {
            this.setPreinstall(_rc);
        }
        else
        if(_rc instanceof IpkgPostInstallResource)
        {
            this.setPostinstall(_rc);
        }
        else
        if(_rc instanceof IpkgPreRemoveResource)
        {
            this.setPreremove(_rc);
        }
        else
        if(_rc instanceof IpkgPostRemoveResource)
        {
            this.setPostremove(_rc);
        }
        else
        {
            sources.add(_rc);
        }
    }

    public ArchiveEnum getFormat() {
        return format;
    }

    public void setFormat(ArchiveEnum format) {
        this.format = format;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public File getTempdir() {
        return tempdir;
    }

    public void setTempdir(File tempdir) {
        this.tempdir = tempdir;
    }

    public ResourceCollection getPreinstall() {
        return preinstall;
    }

    public void setPreinstall(ResourceCollection preinstall) {
        this.preinstall = preinstall;
    }

    public ResourceCollection getPreremove() {
        return preremove;
    }

    public void setPreremove(ResourceCollection preremove) {
        this.preremove = preremove;
    }

    public ResourceCollection getPostinstall() {
        return postinstall;
    }

    public void setPostinstall(ResourceCollection postinstall) {
        this.postinstall = postinstall;
    }

    public ResourceCollection getPostremove() {
        return postremove;
    }

    public void setPostremove(ResourceCollection postremove) {
        this.postremove = postremove;
    }

    public ResourceCollection getControl() {
        return control;
    }

    public void setControl(ResourceCollection control) {
        this.control = control;
    }

    public Resource getDest() {
        return dest;
    }

    public void setDest(Resource dest) {
        this.dest = dest;
    }

    public void setDestfile(File f)
    {
        setDest(new FileResource(f));
    }

    public CompressionEnum getCompression() {
        return compression;
    }

    public void setCompression(CompressionEnum compression) {
        this.compression = compression;
    }
}
