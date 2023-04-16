package com.linbo.feature.runner.core;

import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.TestCase;
import com.linbo.feature.runner.domain.TestcaseResult;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description
 * @Author xbockx
 * @Date 3/26/2023
 */
public interface IRunner {

    Result run(String filename, String path, String input) throws ExecutionException, InterruptedException;

    List<TestcaseResult> runWithTestCase(String filename, String path, List<TestCase> testCases);

}
