package com.linbo.feature.runner.domain;

import lombok.Data;

/**
 * @Description 测试用例
 * @Author xbockx
 * @Date 10/25/2022
 */
@Data
public class TestCase {

    /**
     * 测试用例ID
     */
    private int id;

    /**
     * 题目ID
     */
    private int pid;

    /**
     * 输入
     */
    private String input;

    /**
     * 输出
     */
    private String expOutput;

}
