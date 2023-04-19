package com.linbo.feature.runner.core;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.linbo.feature.runner.Application;
import com.linbo.feature.runner.config.AppConfig;
import com.linbo.feature.runner.domain.Record;
import com.linbo.feature.runner.domain.TestCase;
import com.linbo.feature.runner.util.ProcessBar;
import com.linbo.feature.runner.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * @Description
 * @Author xbockx
 * @Date 10/24/2022
 */
@Slf4j
public class FileService {

    private static volatile FileService INSTANCE;
    CountDownLatch countDownLatch;

    public static FileService getInstance() {
        if (INSTANCE == null) {
            synchronized (FileService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 写入代码文件
     *
     * @param record
     * @return
     * @throws IOException
     */
    public void write(Record record) {
        if (Application.START_MULTI_THREAD) {
            countDownLatch.countDown();
        }
        String wFile = AppConfig.RUNNER_PATH + record.getId() + "/" + record.getId() + ".c";
        if (new File(wFile).exists()) {
            return;
        }
        final File file = new File(AppConfig.RUNNER_PATH + record.getId());
        if (!file.exists()) {
            file.mkdirs();
        }
        try (FileWriter writer = new FileWriter(wFile)) {
            writer.write(record.getCode() == null ? "" :  record.getCode());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 批量写入代码文件
     *
     * @param records
     */
    public void writeCode(List<Record> records) throws InterruptedException {
        log.info("开始写入文件");
        if (Application.START_MULTI_THREAD) {
            // multi-thread
            countDownLatch = new CountDownLatch(records.size());
            for (Record record : records) {
                Runnable runnable = () -> write(record);
                executeTask(runnable);
            }
            countDownLatch.await();
        } else {
            // single-thread
            ProcessBar bar = new ProcessBar(records.size());
            for (int i = 0; i < records.size(); i++) {
                write(records.get(i));
                bar.setProcess(i);
            }
        }
    }

    /**
     * 批量写入测试用例文件
     *
     * @param testCases
     */
    public void writeTestCase(List<TestCase> testCases) {
    }

    /**
     * 删除文件夹
     *
     * @param dir
     */
    public void delete(String dir) {
        File file = new File(AppConfig.RUNNER_PATH + dir);
        file.delete();
    }

    /**
     * 提交任务到线程池
     *
     * @param tasks
     */
    public void executeTasks(List<Runnable> tasks) throws InterruptedException {
        log.info("start write file");
        countDownLatch = new CountDownLatch(tasks.size());
        final ExecutorService threadPool = ThreadPoolUtils.me().getThreadPool();
        for (int i = 0; i < tasks.size(); i++) {
            threadPool.execute(tasks.get(i));
            if (i % 10 == 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        countDownLatch.await();
    }

    /**
     * 提交任务到线程池
     *
     * @param task
     */
    public void executeTask(Runnable task) throws InterruptedException {
        final ExecutorService threadPool = ThreadPoolUtils.me().getThreadPool();
        threadPool.execute(task);
    }


    /**
     * 读取excel
     *
     * @param path
     * @return
     */
    public List<Record> getData(String path) {
        List<Record> list = new ArrayList<>();
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(path, Record.class, new PageReadListener<Record>(dataList -> {
            for (Record record : dataList) {
                log.info("读取到一条Record数据{}", record.getId());
            }
        })).sheet().doRead();
        return list;
    }

    /**
     * 读取测试用例
     *
     * @param path
     * @return
     */
    public Map<Integer, List<TestCase>> getTestCases(String path) {
        Map<Integer, List<TestCase>> map = new HashMap<>();
        EasyExcel.read(path, TestCase.class, new PageReadListener<TestCase>(dataList -> {
            for (TestCase testCase : dataList) {
                log.info("读取到一条TestCase数据{}", testCase.getId());
                List<TestCase> list = map.get(testCase.getPid());
                if (list != null) {
                    list.add(testCase);
                } else {
                    list = new ArrayList<>();
                    list.add(testCase);
                    map.put(testCase.getPid(), list);
                }
            }
        })).sheet().doRead();
        return map;
    }

    /**
     * 读取测试用例
     *
     * @return
     */
    public Map<Integer, List<TestCase>> getTestCases(List<TestCase> testCases) {
        Map<Integer, List<TestCase>> map = new HashMap<>();
        for (TestCase testCase : testCases) {
            log.debug("读取到一条TestCase数据{}", testCase.getId());
            List<TestCase> list = map.get(testCase.getPid());
            if (list != null) {
                list.add(testCase);
            } else {
                list = new ArrayList<>();
                list.add(testCase);
                map.put(testCase.getPid(), list);
            }
        }
        return map;
    }

}
