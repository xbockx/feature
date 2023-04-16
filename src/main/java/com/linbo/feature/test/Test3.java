package com.linbo.feature.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description
 * @Author xbockx
 * @Date 4/13/2023
 */
public class Test3 {

    private static ExecutorService executor = ThreadPoolUtilsTest.me().getThreadPool();
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(20);
        for(int i = 0; i < 20; i++) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("executed");
                countDownLatch.countDown();
            }, "thread-custom-" + i);
            executor.execute(thread);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

}
