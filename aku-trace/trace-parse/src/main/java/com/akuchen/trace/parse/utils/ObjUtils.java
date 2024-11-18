package com.akuchen.trace.parse.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Optional;

@Slf4j
public class ObjUtils {

    public static Object createObj(String className) {
        try {
            Class<?> aClass = ClassUtils.getClass(className);
            Constructor<?>[] constructors = aClass.getDeclaredConstructors();
            Class<?>[] parameterTypes = constructors[0].getParameterTypes();
            Constructor<?> constructor = aClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true); // 如果构造函数是私有的，需要设置可访问性
            return constructor.newInstance();// 实例化对象

        } catch (Exception e) {
            // 处理类不存在的情况
            log.error(String.format("创建对象失败,%s",className), e);
        }
        return null;
    }

    public static Field getField(Object obj, String fieldName) {
       return Optional.ofNullable(getDeclaredField(obj.getClass(), fieldName)).orElseGet(() -> getField(obj.getClass(), fieldName));
    }

    private static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            log.error(String.format("获取字段失败getDeclaredField,%s",fieldName), e);
        }
        return null;
    }
    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            log.error(String.format("获取字段失败getField,%s",fieldName), e);
        }
        return null;
    }


    public static void deleteFilesWithPrefix(String directory, String prefix) {
        File dir = new File(directory);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        };

        File[] files = dir.listFiles(filter);

        if (files != null) {
            for (File file : files) {
                boolean result = file.delete();
                System.out.println("File " + file.getName() + " is deleted: " + result);
            }
        }
    }

}
