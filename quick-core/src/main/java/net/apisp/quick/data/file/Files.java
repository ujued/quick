/**
 * Copyright 2018-present, APISP.NET.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.apisp.quick.data.file;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author UJUED
 * @date 2018-06-11 10:09:45
 */
public class Files {
    /**
     * 二进制数据保存到一个文件
     * 
     * @param data
     * @param path
     * @return
     */
    public static File save(byte[] data, Path path) {
        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            AtomicInteger watcher = new AtomicInteger();
            new AsyncFileWriter(fileChannel, data, 0, watcher).boot();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return path.toFile();
    }
}
