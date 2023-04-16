package com.linbo.feature.runner.service;

import com.linbo.feature.runner.domain.Record;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.TestCase;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author xbockx
 * @Date 12/19/2022
 */
public interface RunService {

    List<Result> exec(List<Record> records, Map<Integer, List<TestCase>> testcaseMap);

}