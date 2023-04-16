package com.linbo.feature.runner.core;

import com.linbo.feature.runner.Application;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.domain.TestcaseResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author xbockx
 * @Date 3/26/2023
 */
@Slf4j(topic = "StringRunner")
public class StringRunner extends AbstractRunner {

    private static volatile IRunner INSTANCE;

    private StringRunner(){}

    public static IRunner getInstance() {
        if (INSTANCE == null) {
            synchronized (StringRunner.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StringRunner();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    protected void judgeTestStatus(TestcaseResult testcaseResult, Result result) {
//        log.info("out: {}, exp: {}", testcaseResult.getOutput(), testcaseResult.getExpect());
        // 检验测试用例
        if (Application.config.getJudgeCase().equals("contain")) {
            if (result.getRunResult().trim().equals(testcaseResult.getExpect().trim())) {
                testcaseResult.setStatus(ResultCode.TEST_PASSED.value);
                return;
            }
            if (result.getRunResult().contains(testcaseResult.getExpect().trim())){
                testcaseResult.setStatus(ResultCode.TEST_PASSED.value);
            } else {
                testcaseResult.setStatus(ResultCode.TEST_FAIL.value);
            }
        } else if (Application.config.getJudgeType().equals("equal")) {
            if (result.getRunResult().equals(testcaseResult.getExpect())) {
                testcaseResult.setStatus(ResultCode.TEST_PASSED.value);
            } else {
                testcaseResult.setStatus(ResultCode.TEST_FAIL.value);
            }
        }

    }
}
