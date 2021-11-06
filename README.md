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
<xtar destfile=".../archive.tar.gz" compression="gzip">
    <tarfileset fullpath="/path/in/archive" file="/path/to/real/file" filemode="555"/>
    <symlink name="/path/in/archive" target="/target/of/symlink" user="root" group="root" permissions="555"/>
</xtar>
```
