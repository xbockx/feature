package com.linbo.feature.runner.util;

import com.linbo.feature.runner.config.BaseConfig;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * @Description
 * @Author xbockx
 * @Date 4/9/2023
 */
@Slf4j
public class ProcessUtils {

    public static void kill(Process process) {
        try {
            if (process != null && process.isAlive()) {
                if (BaseConfig.getOS().toLowerCase().startsWith("win")) {
                    Field f = process.getClass().getDeclaredField("handle");
                    f.setAccessible(true);
                    long handl = f.getLong(process);
                    Kernel32 kernel = Kernel32.INSTANCE;
                    WinNT.HANDLE handle = new WinNT.HANDLE();
                    handle.setPointer(Pointer.createConstant(handl));
                    int ret = kernel.GetProcessId(handle);
                    Long PID = Long.valueOf(ret);
                    String cmd = "cmd /c taskkill /PID " + PID + " /F /T ";
                    Runtime rt = Runtime.getRuntime();
                    Process killPrcess = rt.exec(cmd);
                    killPrcess.waitFor();
                    killPrcess.destroyForcibly();
                    log.debug("{} 超时", PID);
                } else {
                    Field field = process.getClass().getDeclaredField("pid");
                    field.setAccessible(true);
                    long pid = field.getLong(process);
                    String cmd = "/bin/kill -9 " + pid;
                    Runtime rt = Runtime.getRuntime();
                    Process killPrcess = rt.exec(cmd);
                    killPrcess.waitFor();
                    killPrcess.destroyForcibly();
                    log.debug("{} 超时", pid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
