package com.linbo.feature.runner.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description
 * @Author xbockx
 * @Date 4/16/2023
 */
@Slf4j
public class ProcessBar {

    private static final char INCOMPLETE = '░'; // U+2591 Unicode Character 表示还没有完成的部分
    private static final char COMPLETE = '█'; // U+2588 Unicode Character 表示已经完成的部分
    StringBuilder builder;
    float rate;

    public ProcessBar(int total) {
        this.builder = new StringBuilder();
        this.rate = (float) total/ 100;
        Stream.generate(()->INCOMPLETE).limit(100).forEach(builder::append);
    }

    public void setProcess(int curr) {
        curr = (int)(curr / rate);
        builder.replace(curr, curr + 1, String.valueOf(COMPLETE));
        String progressBar = "\r" + builder;
        String percent = " " + (curr + 1) + "%";
        System.out.print(progressBar + percent);
    }

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        List<Integer> collect = Stream.generate(() -> random.nextInt()).limit(256).collect(Collectors.toList());
        ProcessBar processBar = new ProcessBar(collect.size());
        for (int i = 0; i < collect.size(); i++) {
            Thread.sleep(10);
            System.out.print("\rexecute - " + i);
            processBar.setProcess(i);
            Thread.sleep(10);
            System.out.print("\rfinished - " + i);
        }
    }

}
