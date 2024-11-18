package com.akuchen.trace.service.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HashMapUtil {
    private static final Map<String, String> sharedMap = new ConcurrentHashMap<>();

    // 私有构造函数，防止外部实例化
    private HashMapUtil() {}

    // 获取共享的Map实例
    public static Map<String, String> getSharedMap() {
        return sharedMap;
    }

    /**
     * 如果返回值为null，表示键之前不存在，我们返回true；否则返回false。
     * @param key
     * @return
     */
    public static Boolean check(String key) {
        return sharedMap.putIfAbsent(key, String.valueOf(System.currentTimeMillis())) == null;
    }

    public static void del(String key){
        sharedMap.remove(key);
    }

}
