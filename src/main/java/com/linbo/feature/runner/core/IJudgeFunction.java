package com.linbo.feature.runner.core;

import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.TestcaseResult;

/**
 * @Description
 * @Author xbockx
 * @Date 3/26/2023
 */
@FunctionalInterface
public interface IJudgeFunction {
    void judge(TestcaseResult testcaseResult, Result result);
}
