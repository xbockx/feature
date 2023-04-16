package com.linbo.feature.runner.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.string.StringStringConverter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author xbockx
 * @Date 2/5/2022
 */
@Data
public class Result {

    /**
     * 文件名
     */
    @ExcelProperty("文件名")
    private String filename;

    /**
     * 编译结果
     */
    @ExcelProperty("编译结果")
    private String compileResult;

    /**
     * 出错行号
     */
    @ExcelProperty("出错行号")
    private String errorRowNum;

    /**
     * 出错列号
     */
    @ExcelProperty("出错列号")
    private String errorColNum;

    /**
     * 编译状态
     */
    @ExcelProperty("编译状态")
    private Integer compileStatus;

    /**
     * 输入
     * 【无测试用例】
     */
    @ExcelProperty("输入[自定义]")
    private String input;

    /**
     * 运行结果
     * 【无测试用例】
     */
    @ExcelProperty("运行结果[自定义]")
    private String runResult;

    /**
     * 运行状态
     * 【无测试用例】
     */
    @ExcelProperty("运行状态[自定义]")
    private Integer runStatus;

    /**
     * 测试用例原始结果
     * （JSON）
     */
    @ExcelIgnore
    private List<TestcaseResult> testcaseResults;

    /**
     * 通过测试用例数
     */
    @ExcelProperty("通过测试用例数")
    private int passTestNum;

    /**
     * 测试用例总数
     */
    @ExcelProperty("测试用例总数")
    private int testCount;

    /**
     * 测试用例测试状态
     */
    @ExcelProperty("测试用例测试状态")
    private Integer testStatus;

    /**
     * 其他
     */
    @ExcelProperty("其他")
    private String info;

    /**
     * 原始记录
     * 不作为结果
     */
    @ExcelIgnore
    private Record source;

}
