package com.linbo.feature.runner.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linbo.feature.runner.Application;
import com.linbo.feature.runner.config.AppConfig;
import com.linbo.feature.runner.core.Compiler;
import com.linbo.feature.runner.core.IRunner;
import com.linbo.feature.runner.core.NumericRunner;
import com.linbo.feature.runner.core.StringRunner;
import com.linbo.feature.runner.domain.*;
import com.linbo.feature.runner.service.RunService;
import com.linbo.feature.runner.util.ProcessBar;
import com.linbo.feature.runner.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description
 * @Author xbockx
 * @Date 12/19/2022
 */
@Slf4j
public class RunServiceImpl implements RunService {

    private Compiler compiler = Compiler.getInstance();

    private IRunner runner = NumericRunner.getInstance();

    private ObjectMapper objectMapper = new ObjectMapper();

    private final ExecutorService threadPool = ThreadPoolUtils.me().getThreadPool();

    CountDownLatch countDownLatch;

    public RunServiceImpl() {
        if (Application.config.getJudgeType().equals("string")) {
            runner = StringRunner.getInstance();
        }
    }

    @Override
    public List<Result> exec(List<Record> records, Map<Integer, List<TestCase>> testcaseMap) {
        // compile
        log.info("开始编译程序");
        List<Result> results = new ArrayList<>();
        if (Application.START_MULTI_THREAD) {
            // multi-thread start
            List<FutureTask<Result>> compileTasks = new ArrayList<>(1000);
            for (Record record : records) {
                FutureTask task = new FutureTask(() -> {
                    log.debug("[TASK]: 开始编译任务: {}", record.getId());
                    String path = AppConfig.RUNNER_PATH + record.getId() + "/";
                    String filename = String.valueOf(record.getId());
                    final Result compile = compiler.compile(filename, path);
                    compile.setSource(record);
                    log.debug("[compile] thread: {}, filename: {}", Thread.currentThread(), filename);
                    return compile;
                });
                compileTasks.add(task);
                threadPool.submit(task);
            }
            for (FutureTask<Result> compileTask : compileTasks) {
                try {
                    final Result result = compileTask.get();
                    log.info("[TASK]: 完成编译任务: {}", result.getSource().getId());
                    results.add(result);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // single-thread start
            ProcessBar bar = new ProcessBar(records.size());
            for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);
                String path = AppConfig.RUNNER_PATH + record.getId() + "/";
                String filename = String.valueOf(record.getId());
                final Result compile = compiler.compile(filename, path);
                compile.setSource(record);
                results.add(compile);
                bar.setProcess(i);
            }
        }
        log.info("所有文件编译完成");

        // run
        log.info("开始运行程序");
        if (Application.START_MULTI_THREAD) {
            countDownLatch = new CountDownLatch(results.size());
            for (Result result : results) {
                Runnable runnable = () -> {
                    log.debug("[TASK]: 开始执行任务: {}", result.getFilename());
                    if (result.getCompileStatus() == ResultCode.COMPILE_SUCCESS.value) {
                        String path = AppConfig.RUNNER_PATH + result.getSource().getId() + "/";
                        List<TestCase> testCases = testcaseMap.get(result.getSource().getPid());
                        final List<TestcaseResult> list = runner.runWithTestCase(result.getFilename(), path, testCases);
                        int passedTestCount = (int) list.stream().filter(testcaseResult -> testcaseResult.getStatus() == ResultCode.TEST_PASSED.value).count();
                        result.setTestcaseResults(list);
                        result.setTestCount(testCases.size());
                        result.setPassTestNum(passedTestCount);
                        result.setTestStatus(111);
                        try {
                            result.setInfo(objectMapper.writeValueAsString(list));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        log.debug("[run] thread: {}, filename: {}, count: {}", Thread.currentThread(), result.getFilename(), countDownLatch.getCount());
                    }
                    countDownLatch.countDown();
                };
                threadPool.submit(runnable);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // single-thread start
            ProcessBar bar = new ProcessBar(records.size());
            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                if (result.getCompileStatus() == ResultCode.COMPILE_SUCCESS.value) {
                    String path = AppConfig.RUNNER_PATH + result.getSource().getId() + "/";
                    List<TestCase> testCases = testcaseMap.get(result.getSource().getPid());
                    final List<TestcaseResult> list = runner.runWithTestCase(result.getFilename(), path, testCases);
                    int passedTestCount = (int) list.stream().filter(testcaseResult -> testcaseResult.getStatus() == ResultCode.TEST_PASSED.value).count();
                    result.setTestcaseResults(list);
                    result.setTestCount(testCases.size());
                    result.setPassTestNum(passedTestCount);
                    result.setTestStatus(111);
                    try {
                        result.setInfo(objectMapper.writeValueAsString(list));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                bar.setProcess(i);
            }
        }
        log.info("所有文件运行完成");

        // end
        threadPool.shutdown();
        return results;
    }

}
