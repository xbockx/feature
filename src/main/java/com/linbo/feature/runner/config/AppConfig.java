package com.linbo.feature.runner.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @Description
 * @Author xbockx
 * @Date 10/24/2022
 */
@Slf4j(topic = "AppConfig")
public class AppConfig{

    public static String WORK_DIR = "C:/temp/";

    public static String RUNNER_PATH = "C:/temp/runner/";

    public static Long COMPILE_TIME = 500L;

    public static Long RUN_TIME = 300L;

    public AppConfig() {
    }

    public AppConfig(String workDir) {
        AppConfig.WORK_DIR = workDir + "/";
        AppConfig.RUNNER_PATH = AppConfig.WORK_DIR + "/.runner/";
        updateWorkSpace();
    }

    public AppConfig(String runnerPath, Long compileTime, Long runTime) {
        AppConfig.RUNNER_PATH = runnerPath;
        AppConfig.COMPILE_TIME = compileTime;
        AppConfig.RUN_TIME = runTime;
        updateWorkSpace();
    }

    public static void updateWorkSpace() {
        File dir = new File(RUNNER_PATH);
        if (dir.exists()) {
            dir.delete();
        }
        dir.mkdirs();
        log.info("Workspace Update... OK!");
    }


}
