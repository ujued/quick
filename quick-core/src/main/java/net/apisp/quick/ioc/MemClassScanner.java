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

package net.apisp.quick.ioc;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface MemClassScanner extends ClassScanner {

    Set<Class<?>> classpathClasses();

    @Override
    default Class<?>[] get(Class<?> cls) {
        return new Class[0];
    }

    @Override
    default Class<?>[] getByAnnotation(Class<? extends Annotation> anno) {
        List<Class<?>> clses = new ArrayList<>();
        Iterator<Class<?>> clsIter = classpathClasses().iterator();
        if (!anno.isAnnotation()) {
            return null;
        }
        while (clsIter.hasNext()) {
            Class<?> class1 = clsIter.next();
            if (class1.getAnnotation(anno) != null) {
                clses.add(class1);
            }
        }
        Class<?>[] clss = new Class<?>[clses.size()];
        for (int i = 0; i < clss.length; i++) {
            clss[i] = clses.get(i);
        }
        return clss;
    }

    @Override
    default  <T> Class<T>[] getByInterface(Class<T> ifce) {
        List<Class<?>> clses = new ArrayList<>();
        Iterator<Class<?>> clsIter = classpathClasses().iterator();
        if (!ifce.isInterface()) {
            return null;
        }
        while (clsIter.hasNext()) {
            Class<?> class1 = clsIter.next();
            if (ifce.isAssignableFrom(class1)) {
                clses.add(class1);
            }
        }
        Class<?>[] clss = new Class<?>[clses.size()];
        for (int i = 0; i < clss.length; i++) {
            clss[i] = clses.get(i);
        }
        return (Class<T>[]) clss;
    }
}
