### ANT Archive Plus

Enhances the Ant-Compress.

#### Tar Task

* allows tar task to transparently be gzipped
* allows symlink resources for creating synthetic symlinks.

##### Declaration

```xml
<taskdef name="xtar" classname="com.github.terefang.ant.archiveplus.taskdefs.Tar" />
<typedef name="symlink" classname="com.github.terefang.ant.archiveplus.resources.SymbolicLink" />
```

##### Usage

```xml
<xtar destfile=".../archive.tar...." compression="gzip|bzip2|xz|zstd">
    <tarfileset fullpath="/path/in/archive" file="/path/to/real/file" filemode="555"/>
    <symlink name="/path/in/archive" target="/target/of/symlink" user="root" group="root" permissions="555"/>
</xtar>
```

#### ipkg Task

creates ipkg/opkg package files.

##### Declaration

```xml
<typedef resource="antarchiveplus/antlib.xml" />
```

##### Usage

```xml
<ipkg destfile=".../test.ipkg">
    <ipkg-control packageName="${artifactId}" version="${version}" architecture="x86_64"/>
    <ipkg-preinstall file="..." />
    <ipkg-postinstall file="..." />
    <ipkg-preremove file="..." />
    <ipkg-postremove file="..." />
    <tarfileset prefix="/path/to/prefix/" dir="${basedir}/src/main/dist/xbin/" >
        <include name="bin-file" />
    </tarfileset>
    <tarfileset fullpath="/path/to/prefix/some-cmd" file="${.basedir}/some-cmd-other-name" />
</ipkg>
```

