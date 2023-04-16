package com.linbo.feature.runner.domain;

/**
 * @Description
 * @Author xbockx
 * @Date 2/5/2022
 */
public enum ResultCode {
    // 编译通过
    COMPILE_SUCCESS(200, "编译通过"),
    // 编译失败
    COMPILE_FAIL(201, "编译失败"),

    // 运行成功
    RUN_SUCCESS(300, "运行成功"),
    // 运行失败
    RUN_FAIL(301, "运行失败"),
    // 运行超时
    RUN_TIMEOUT(302, "运行超时"),

    // 通过单测试用例
    TEST_PASSED(400, "通过该测试用例"),
    // 未通过单测试用例
    TEST_FAIL(401, "未通过该测试用例"),
    // 恶意输出
    TEST_MALICIOUS_OUTPUT(402, "恶意输出"),

    // 测试用例全部通过
    ACCEPT(500, "通过"),
    // 测试用例未全部通过
    PART_ACCPET(501, "部分通过"),
    // 测试用例全未通过
    REJECT(502, "未通过");

    public int value;

    public String msg;

    ResultCode(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }
}
