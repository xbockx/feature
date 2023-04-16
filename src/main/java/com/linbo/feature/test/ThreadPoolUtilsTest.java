package com.linbo.feature.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @Description
 * @Author xbockx
 * @Date 10/20/2022
 */
@Slf4j
public class ThreadPoolUtilsTest {

    private static ExecutorService service;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private ThreadPoolUtilsTest() {
        service = new ThreadPoolExecutor(
                1, // 核心线程数
                2, // 最大线程数。最多几个线程并发。
                3,//当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(1), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new MyRejectPolicy()
        );
    }

    private static class Inner {
        public static final ThreadPoolUtilsTest INSTANCE = new ThreadPoolUtilsTest();
    }

    public static ThreadPoolUtilsTest me() {
        return Inner.INSTANCE;
    }

    public ExecutorService getThreadPool() {
        return service;
    }

    class MyRejectPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.info("拒绝： {}", ((Thread) r).getName());
        }
    }

}
