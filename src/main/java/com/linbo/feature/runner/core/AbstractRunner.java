package com.linbo.feature.runner.core;

import com.linbo.feature.runner.config.AppConfig;
import com.linbo.feature.runner.config.BaseConfig;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.domain.TestCase;
import com.linbo.feature.runner.domain.TestcaseResult;
import com.linbo.feature.runner.util.ProcessUtils;
import com.linbo.feature.runner.util.StreamUtils;
import com.linbo.feature.runner.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Description
 * @Author xbockx
 * @Date 3/26/2023
 */
@Slf4j(topic = "AbstractRunner")
public abstract class AbstractRunner implements IRunner {

    protected static final long INTERVAL = AppConfig.RUN_TIME;

    public static final ExecutorService THREAD_POOL = ThreadPoolUtils.me().getThreadPool();

    /**
     * 运行程序
     *
     * @param filename
     * @param path
     * @param input
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Result run(String filename, String path, String input) throws ExecutionException, InterruptedException {
        Process process = null;
        BufferedWriter stdIn;
        BufferedReader stdOut = null;
        BufferedReader stdError = null;
        String outResult;
        Result result = new Result();
        try {
            if (BaseConfig.getOS().toLowerCase().startsWith("win")) {
                process = Runtime.getRuntime().exec("cmd /c " + filename, null, new File(path));
            } else {
                process = Runtime.getRuntime().exec("./" + filename, null, new File(path));
            }

            log.debug("[run]: filename: {}, input: {}", filename, input);

            final TimeControl timeControl = new TimeControl(process, 500L, TimeUnit.MILLISECONDS);
            THREAD_POOL.execute(timeControl);

            stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            stdIn = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            if (input != null) {
                StreamUtils.writeStream(stdIn, input);
            }

            outResult = StreamUtils.readStream2(stdOut, INTERVAL);

//            log.info("[run]: filename: {}, input: {}, output: {}", filename, input, outResult);
            log.debug("[run]: filename: {}, output: {}", filename, outResult);

            if (outResult.isEmpty()) {
                log.debug("[result empty]: {}: {}", filename, input);
            }
            result.setRunResult(outResult);
            process.destroy();
        } catch (IOException e) {
            log.error("执行命令或变量错误！", e);
        } catch (Exception e) {
            log.error("执行异常： {}", e.getMessage());
            result.setRunResult(e.getMessage());
        } finally {
            StreamUtils.close(stdOut);
            StreamUtils.close(stdError);
            ProcessUtils.kill(process);
        }
        return result;
    }

    /**
     * 运行并测试
     *
     * @param filename
     * @param path
     * @param testCases
     * @return
     */
    @Override
    public List<TestcaseResult> runWithTestCase(String filename, String path, List<TestCase> testCases) {
        List<TestcaseResult> testcaseResults = new ArrayList<>();
        for (TestCase testCase : testCases) {
            TestcaseResult testcaseResult = new TestcaseResult();
            try {
                testcaseResult.setInput(testCase.getInput());
                testcaseResult.setExpect(testCase.getExpOutput());
                final Result run = run(filename, path, testCase.getInput());
                testcaseResult.setOutput(run.getRunResult());
                judgeTestStatus(testcaseResult, run);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                testcaseResult.setStatus(ResultCode.TEST_FAIL.value);
            }
            testcaseResults.add(testcaseResult);
        }
        return testcaseResults;
    }

    protected abstract void judgeTestStatus(TestcaseResult testcaseResult, Result result);

}
