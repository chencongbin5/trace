package com.akuchen.trace.service.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {

    private static final int DEFAULT_POOL_SIZE = 10;

    private static ExecutorService executorService;

    private ThreadPoolUtils() {
        // 私有构造函数，防止外部实例化
    }

    /**
     * 获取线程池实例
     *
     * @return 线程池实例
     */
    public static synchronized ExecutorService getExecutorService() {
        if (executorService == null) {
            // 使用固定大小的线程池，可以根据需求选择其他类型的线程池
            executorService = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        }
        return executorService;
    }

    /**
     * 关闭线程池
     */
    public static synchronized void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 提交任务给线程池执行
     *
     * @param task 任务
     */
    public static void submitTask(Runnable task) {
        getExecutorService().submit(task);
    }
}

