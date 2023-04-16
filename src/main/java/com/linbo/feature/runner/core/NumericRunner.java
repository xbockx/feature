package com.linbo.feature.runner.core;

import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.domain.TestcaseResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author xbockx
 * @Date 3/26/2023
 */
@Slf4j(topic = "NumericRunner")
public class NumericRunner extends AbstractRunner {

    private static volatile IRunner INSTANCE;

    private NumericRunner(){}

    public static IRunner getInstance() {
        if (INSTANCE == null) {
            synchronized (NumericRunner.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NumericRunner();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    protected void judgeTestStatus(TestcaseResult testcaseResult, Result result) {
//        log.info("out: {}, exp: {}", testcaseResult.getOutput(), testcaseResult.getExpect());
        // 判断恶意输出
        // TODO add rules
        if (result.getSource() != null && result.getSource().getCode() != null) {
            final String[] split = result.getSource().getCode().split("\r");
            for (String str : split) {
                if (str.trim().matches(".*printf")) {
                    final String substring = str.substring(str.indexOf("printf(") + 7);
                    if (substring.contains(testcaseResult.getExpect())) {
                        testcaseResult.setStatus(ResultCode.TEST_MALICIOUS_OUTPUT.value);
                        return;
                    }
                }
            }
        }
        // 检验测试用例
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
