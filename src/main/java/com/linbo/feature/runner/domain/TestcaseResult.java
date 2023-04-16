package com.linbo.feature.runner.domain;

import lombok.Data;

/**
 * @Description
 * @Author xbockx
 * @Date 2/18/2023
 */
@Data
public class TestcaseResult {

    /**
     * 测试输入
     */
    private String input;

    /**
     * 实际输出
     */
    private String output;

    /**
     * 期待结果
     */
    private String expect;

    /**
     * 单测试状态
     */
    private int status;

}
