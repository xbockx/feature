package com.linbo.feature.runner.config;

import lombok.Data;
import lombok.ToString;

/**
 * @Description
 * @Author xbockx
 * @Date 4/8/2023
 */
@ToString
@Data
public class BaseConfig {

    // 执行文件路径
    private String executePath;

    // 测试用例文件路径
    private String testcasePath;

    // 根路径
    private String rootPath;

    // 导出结果路径
    private String exportPath;

    // 导出结果名
    private String name;

    // 是否启动多线程
    private boolean startMultiThread;

    // 判题类型
    // number / string
    private String judgeType;

    // 判题条件
    // contain / equal
    private String judgeCase;

    public static String getOS() {
        return System.getProperty("os.name");
    }

}
