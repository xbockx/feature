package com.linbo.feature.runner.domain;

import lombok.Data;

/**
 * @Description
 * @Author xbockx
 * @Date 10/24/2022
 */
@Data
public class Record {

    /**
     * 记录ID
     */
    private int id;

    /**
     * 题目ID
     */
    private int pid;

    /**
     * 代码
     */
    private String code;

    private String studentId;

}
