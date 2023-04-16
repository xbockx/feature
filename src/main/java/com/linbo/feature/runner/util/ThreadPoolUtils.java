package com.linbo.feature.runner.util;

import java.util.concurrent.*;

/**
 * @Description
 * @Author xbockx
 * @Date 10/20/2022
 */
public class ThreadPoolUtils {

    private static ExecutorService service;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private ThreadPoolUtils() {
        service = new ThreadPoolExecutor(
                cpuNum, // 核心线程数
                2 * cpuNum + 1, // 最大线程数。最多几个线程并发。
                3,//当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(200 * cpuNum), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    private static class Inner {
        public static final ThreadPoolUtils INSTANCE = new ThreadPoolUtils();
    }

    public static ThreadPoolUtils me() {
        return Inner.INSTANCE;
    }

    public ExecutorService getThreadPool() {
        return service;
    }

    class MyRejectPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println();
        }
    }

}
