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
package net.apisp.quick.server;

import net.apisp.quick.annotation.ReflectionCall;
import net.apisp.quick.annotation.Unfulfilled;
import net.apisp.quick.config.Configuration;
import net.apisp.quick.core.http.*;
import net.apisp.quick.core.http.annotation.ResponseType;
import net.apisp.quick.ioc.Container;
import net.apisp.quick.ioc.def.SimpleContainer;
import net.apisp.quick.log.Log;
import net.apisp.quick.log.def.LogFactory;
import net.apisp.quick.server.std.QuickContext;
import net.apisp.quick.server.std.QuickServer;
import net.apisp.quick.support.lang.ArgRunnable;
import net.apisp.quick.support.lang.BiArgsRunnable;
import net.apisp.quick.support.lang.FastRouter;
import net.apisp.quick.thread.TaskExecutor;
import net.apisp.quick.util.Classpaths;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server上下文
 *
 * @author UJUED
 * @date 2018-06-08 09:05:07
 */
public class ServerContext implements QuickContext {
    private static final Log LOG = LogFactory.getLog(ServerContext.class);
    private static QuickContext instance;
    private Map<String, RequestExecutorInfo> mappings = new HashMap<>();
    private Map<Pattern, RequestExecutorInfo> regMappings = new HashMap<>();
    private TaskExecutor executor;
    private Class<QuickServer> serverClass;
    private boolean normative = true;

    private boolean crossDomain = false;

    private Map<String, String> defaultRespHeaders = new HashMap<>();

    private Container container = new SimpleContainer();

    private ServerContext() {
        serverClass = Classpaths.safeLoadClass(Configuration.get("server").toString(), QuickServer.class);
        executor = TaskExecutor.create((int) Configuration.get("server.threads"));
        defaultRespHeaders.put("Connection", "keep-alive");

        // 缓存指定的控制器统一异常处理器
        String cehName = (String) Configuration.get("exception.handler");
        Class<? extends ExceptionHandler> h = Classpaths.safeLoadClass(cehName, ExceptionHandler.class);
        try {
            this.accept(ExceptionHandler.class.getName(), Injections.inject(h.newInstance(), this.container));
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.warn("Not suitable ExceptionHandler class {} .", h.getName());
        }

        // 缓存快速Mapping支持
        this.accept(new FastRouter());
    }

    /**
     * 初始化一个QuickContext
     *
     * @return
     */
    public static synchronized QuickContext init() {
        if (instance == null) {
            instance = new ServerContext();
            instance.accept(QuickContext.class.getName(), instance);
        }
        return instance;
    }

    /**
     * 尝试获取ServerContext， 在脱离QuickServer环境调用时，返回null值
     *
     * @return
     */
    public static QuickContext tryGet() {
        return instance;
    }

    @Override
    public boolean isCors() {
        return crossDomain;
    }

    @ReflectionCall("net.apisp.quick.server.QuickServerThread.run()")
    private void setNormative(Boolean normative) {
        this.normative = normative;
    }

    @ReflectionCall("net.apisp.quick.server.MappingResolver.prepare()")
    private void setCrossDomain(Boolean crossDomain) {
        this.crossDomain = crossDomain;
    }

    @ReflectionCall("net.apisp.quick.support.SupportController.unloadSingleton()")
    private void unloadSingleton(String name) {
        this.container.unload(name);
    }

    @Override
    public TaskExecutor executor() {
        return executor;
    }

    @Override
    public String charset() {
        return (String) Configuration.get("charset");
    }

    @Override
    public <T> T singleton(Class<T> type) {
        return container.singleton(type);
    }

    @Override
    public Object singleton(String name) {
        return container.singleton(name);
    }

    @Override
    public Object setting(String key) {
        return Configuration.get(key);
    }

    @Override
    public Path tmpDirPath(String... more) {
        return Paths.get((String) Configuration.get("server.tmp.dir"), more);
    }

    @Override
    public int port() {
        return (int) Configuration.get("server.port");
    }

    @Override
    public Class<QuickServer> serverClass() {
        return serverClass;
    }

    @Override
    public Map<String, String> responseHeaders() {
        return defaultRespHeaders;
    }

    @Override
    public RequestExecutorInfo hit(String method, String uri) {
        String key = method.toUpperCase() + " " + uri;
        RequestExecutorInfo info = mappings.get(key);
        if (info != null) {
            return info;
        }
        Iterator<Map.Entry<Pattern, RequestExecutorInfo>> entryIter = regMappings.entrySet().iterator();
        Map.Entry<Pattern, RequestExecutorInfo> entry;
        while (entryIter.hasNext()) {
            entry = entryIter.next();
            if (key.matches(entry.getKey().pattern())) {
                info = entry.getValue();
                Matcher matcher = entry.getKey().matcher(key);
                matcher.find();
                for (int i = 0; i < matcher.groupCount(); i++) {
                    info.addPathVariable(matcher.group(i + 1), i);
                }
                return info;
            }
        }
        return info;
    }

    @Override
    public boolean isNormative() {
        return normative;
    }

    @Override
    public QuickContext mapping(String key, RequestExecutorInfo executeInfo) {
        LOG.info("Mapping {} : {}", key, executeInfo.getMethod().toGenericString());
        if (key.indexOf('{') < key.indexOf('}')) {
            String[] md_uri = key.split(" ");
            String httpMethod = md_uri[0];
            String uri = md_uri[1];
            StringBuilder regString = new StringBuilder(httpMethod + "\\s");
            StringBuilder varName = new StringBuilder();
            char[] segment = uri.trim().toCharArray();
            boolean recordStart = false;
            for (int p = 0; p < segment.length; p++) {
                if (segment[p] == '{') {
                    recordStart = true;
                    continue;
                } else if (segment[p] == '}') {
                    recordStart = false;
                    regString.append("([^/]*)?");
                    executeInfo.addPathVariableName(varName.toString());
                    varName.delete(0, varName.length());
                    continue;
                } else if (recordStart) {
                    varName.append(segment[p]);
                    continue;
                } else if (segment[p] == '/') {
                    regString.append("/+");
                    continue;
                } else {
                    regString.append(segment[p]);
                }
            }
            this.regMappings.put(Pattern.compile(regString.toString()), executeInfo);
            return this;
        }
        mappings.put(key, executeInfo);
        return this;
    }

    @Override
    public QuickContext mapping(String key, Function<WebContext, Object> executor) {
        return mapping0(key, executor);
    }

    @Override
    public QuickContext mapping(String key, BiArgsRunnable<HttpRequest, HttpResponse> executor) {
        return mapping0(key, executor);
    }

    @Override
    public QuickContext mapping(String key, Supplier<Object> executor) {
        return mapping0(key, executor);
    }

    @Override
    public QuickContext mapping(String key, Runnable executor) {
        return mapping0(key, executor);
    }

    @Override
    public QuickContext mapping(String key, ArgRunnable<WebContext> executor) {
        return mapping0(key, executor);
    }

    @Override
    public QuickContext mapping(String key, Class<?> controller, String methodName, Class<?>... paramTypes) {
        try {
            Method method = controller.getDeclaredMethod(methodName, paramTypes);
            ResponseType responseType;
            if ((responseType = method.getAnnotation(ResponseType.class)) == null) {
                responseType = new ResponseType() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ResponseType.class;
                    }

                    @Override
                    public String value() {
                        return ContentTypes.JSON;
                    }
                };
            }
            RequestExecutorInfo info = new RequestExecutorInfo(method, this.singleton(controller));
            info.addHeader("Content-Type", responseType.value() + "; charset=" + this.charset());
            mapping(key, info);
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.warn(e.getMessage());
        }
        return this;
    }

    @Override
    public <T> T accept(T obj) {
        this.container.accept(obj);
        return obj;
    }

    @Override
    public <T> T accept(String name, T obj) {
        this.container.accept(name, obj);
        return obj;
    }

    @Override
    public Set<String> objects() {
        return this.container.objects();
    }

    @Override
    public <T> T singleton(Class<T> type, boolean safe) {
        return this.container.singleton(type, safe);
    }

    @Override
    public Object singleton(String name, boolean safe) {
        return this.container.singleton(name, safe);
    }

    @Override
    public void accept(String name, ObjectCreaterUnit unit) {
        this.container.accept(name, unit);
    }

    @Override
    public ThreadLocal<?> safeSingleton(String name) {
        return this.container.safeSingleton(name);
    }

    @Override
    public boolean contains(Class<?> type) {
        return this.container.contains(type);
    }

    @Override
    public boolean contains(String name) {
        return this.container.contains(name);
    }

    @Unfulfilled
    @Override
    public void unload(String name) {
    }

    @Unfulfilled
    @Override
    public void unload(Class<?> type) {
    }

    /**
     * 映射抽象的请求处理器
     *
     * @param key
     * @param executor
     * @return
     */
    private QuickContext mapping0(String key, Object executor) {
        RequestExecutorInfo executeInfo = new RequestExecutorInfo();
        try {
            executeInfo.setMethod(FastRouter.class.getDeclaredMethod("route",
                    HttpRequest.class, HttpResponse.class, WebContext.class, Object.class));
            executeInfo.setObject(this.singleton(FastRouter.class));
            this.accept(executeInfo.toString(), executor);
        } catch (NoSuchMethodException | SecurityException e) {
            LOG.warn("Lost mapping {}", key);
            return this;
        }
        this.mapping(key, executeInfo);
        return this;
    }
}
