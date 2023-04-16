package com.linbo.feature.entity.config;

import lombok.Data;

/**
 * @Description
 * @Author xbockx
 * @Date 2/16/2023
 */
@Data
public class Execute {

    private int executeId;

    private int exerciseId;

    private String code;

    private String input;

    private String compileResult;

    private String runResult;

    private String time;

    private int status;

    private float similarity;

    private int studentId;

    private String studentNum;

    private int teacherId;

    private String teacherNum;

    private long createdTime;

    private String courseNum;

    private String sourceIp;

}
