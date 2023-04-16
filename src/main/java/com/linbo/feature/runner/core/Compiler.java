package com.linbo.feature.runner.core;

import com.linbo.feature.runner.config.AppConfig;
import com.linbo.feature.runner.config.BaseConfig;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.util.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

/**
 * @Description
 * @Author xbockx
 * @Date 10/20/2022
 */
@Slf4j(topic = "Compiler")
public class Compiler {

    private static volatile Compiler INSTANCE;

    private static final long INTERVAL = AppConfig.COMPILE_TIME;

    private Compiler(){}

    public static Compiler getInstance() {
        if (INSTANCE == null) {
            synchronized (Compiler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Compiler();
                }
            }
        }
        return INSTANCE;
    }
    /**
     * 编译
     *
     * @return
     */
    public Result compile(String filename, String path) {
        Process process = null;
        BufferedReader stdOut = null;
        BufferedReader stdError = null;
        String outResult = null;
        String errorResult = null;
        Result result = new Result();
        try {
            String runFilename = "R" + filename;
            if (BaseConfig.getOS().toLowerCase().startsWith("win")) {
                process = Runtime.getRuntime().exec("cmd /c gcc -o " + runFilename + " " + filename + ".c", null, new File(path));
            } else {
                process = Runtime.getRuntime().exec("gcc -o " + runFilename + " " + filename + ".c", null, new File(path));
            }
            if (process != null) {
                stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                outResult = StreamUtils.readStream(stdOut, INTERVAL);
                errorResult = StreamUtils.readStream(stdError, INTERVAL);

                if (errorResult.isEmpty()) {
                    result.setCompileStatus(ResultCode.COMPILE_SUCCESS.value);
                    result.setCompileResult(outResult);
                    result.setFilename(runFilename);
                } else {
                    result.setCompileStatus(ResultCode.COMPILE_FAIL.value);
                    result.setCompileResult(errorResult);
                    result.setFilename(runFilename);
                }
                process.destroy();
            }
        } catch (IOException e) {
            log.error("执行命令或变量错误！", e);
        } catch (TimeoutException e) {
            result.setCompileStatus(ResultCode.COMPILE_FAIL.value);
            result.setCompileResult("Timeout");
        } finally {
            StreamUtils.close(stdOut);
            StreamUtils.close(stdError);
        }
        return result;
    }

}
