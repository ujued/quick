package net.apisp.quick.ioc.def;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class NewClassScanner extends FsClassScanner{

    public NewClassScanner(URI uri, String basePackageName) {
        super(uri, basePackageName);
    }

    /**
     * 自定义类加载。无缓存，每次加载最新的类
     *
     * @param rootPath
     * @param path
     * @return
     */
    @Override
    public Class<?> loadClass(Path rootPath, Path path) {
        byte[] classBin = null;
        try {
            classBin = Files.readAllBytes(path);
        } catch (IOException e) {
            // impossible
        }
        return this.defineClass(getClassNameByPath(rootPath, path), classBin, 0, classBin.length);
    }
}
