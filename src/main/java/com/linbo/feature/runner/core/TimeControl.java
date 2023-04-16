package com.linbo.feature.runner.core;

import com.linbo.feature.runner.util.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author xbockx
 * @Date 4/9/2023
 */
@Slf4j
public class TimeControl implements Runnable{

    Process process;

    Long time;

    TimeUnit timeUnit;

    public TimeControl(Process process, Long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
        this.process = process;
    }

    @Override
    public void run() {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process.isAlive()) {
                try {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProcessUtils.kill(process);
            }
        }
    }
}
