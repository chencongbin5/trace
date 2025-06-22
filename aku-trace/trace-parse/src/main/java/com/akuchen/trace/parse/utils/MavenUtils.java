package com.akuchen.trace.parse.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MavenUtils {

    // 获取 Maven 项目的所有依赖项路径
    public static String getMavenDependencies() {
        // 获取当前线程的类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 获取类路径的URL数组
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        // 遍历URL数组打印类路径
        return  Arrays.stream(urls).map(URL::getFile).collect(Collectors.joining(File.pathSeparator));
    }
}
