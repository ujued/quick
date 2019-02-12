/**
 * Copyright (c) 2018 Ujued and APISP.NET. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.apisp.quick.ioc.def;

import net.apisp.quick.ioc.ClassScanner;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.def.LogFactory;

import java.net.URI;
import java.nio.file.Path;

/**
 * 简单的类扫描器
 *
 * @author Ujued
 * @date 2018-06-15 11:08:11
 */
public class SimpleClassScanner extends FsClassScanner {

    private static final Log LOG = LogFactory.getLog(SimpleClassScanner.class);

    public SimpleClassScanner(URI uri, String basePackageName) {
        super(uri, basePackageName);
    }

    public static ClassScanner create(URI userBin, String packageName) {
        return new SimpleClassScanner(userBin, packageName);
    }

    /**
     * 系统加载器
     *
     * @param path
     * @return
     */
    @Override
    public Class<?> loadClass(Path rootPath, Path path) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(getClassNameByPath(rootPath, path));
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}