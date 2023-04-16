package com.linbo.feature.runner.core;

import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.domain.TestcaseResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author xbockx
 * @Date 10/20/2022
 */
@Slf4j(topic = "DefaultRunner")
public class DefaultRunner extends AbstractRunner{

    private static volatile IRunner INSTANCE;

    private DefaultRunner(){}

    public static IRunner getInstance() {
        if (INSTANCE == null) {
            synchronized (DefaultRunner.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DefaultRunner();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected void judgeTestStatus(TestcaseResult testcaseResult, Result result) {
        if (result.getRunResult().equals(testcaseResult.getExpect())) {
            testcaseResult.setStatus(ResultCode.TEST_PASSED.value);
            return;
        }
        if (result.getRunResult().trim().equals(testcaseResult.getExpect().trim())){
            testcaseResult.setStatus(ResultCode.TEST_PASSED.value);
        } else {
            testcaseResult.setStatus(ResultCode.TEST_FAIL.value);
        }
    }
}
