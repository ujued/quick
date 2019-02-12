package net.apisp.quick.ioc.def;

import net.apisp.quick.ioc.MemClassScanner;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.def.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class FsClassScanner extends ClassLoader implements MemClassScanner {

    private static final Log LOG = LogFactory.getLog(FsClassScanner.class);
    private Set<Class<?>> classes = new HashSet<>();
    private Path rootPath;

    public FsClassScanner(URI uri, String basePackageName) {
        if (uri.getScheme().equals("jar")) {
            try {
                FileSystem zipfs = FileSystems.newFileSystem(uri, new HashMap<>());
                this.rootPath = zipfs.getPath("/");
            } catch (FileSystemAlreadyExistsException e) {
                this.rootPath = Paths.get(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.rootPath = Paths.get(uri);
        }
        collect(this.rootPath.resolve(basePackageName.replace('.', '/')));
    }

    @Override
    public Set<Class<?>> classpathClasses() {
        return classes;
    }

    /**
     * 收集路径下的类
     *
     * @param root
     * @return
     */
    private Set<Class<?>> collect(Path root) {
        Stream<Path> list = null;
        try {
            list = Files.list(root);
            list.forEach(path -> {
                if (!path.toString().endsWith(".class")) {
                    collect(path);
                    return;
                }
                try {
                    classes.add(loadClass(rootPath, path));
                } catch (LinkageError error) {
                    LOG.error(error.getMessage());
                }
            });

        } catch (IOException e) {
        } finally {
            if (list != null) {
                list.close();
            }
        }
        return classes;
    }

    public abstract Class<?> loadClass(Path rootPath, Path path);

    /**
     * 由PATH解析出类名
     *
     * @param rootPath
     * @param t
     * @return
     */
    public String getClassNameByPath(Path rootPath, Path t) {
        String tmp = t.subpath(rootPath.getNameCount(), t.getNameCount()).toString();
        return tmp.substring(0, tmp.length() - 6).replace('\\', '.').replace('/', '.');
    }
}
