package com.linbo.feature.runner.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Description
 * @Author xbockx
 * @Date 10/20/2022
 */
@Slf4j(topic = "StreamUtils")
public class StreamUtils {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("IO关闭异常", e);
            }
        }
    }

    public static String readStream2(BufferedReader reader, long interval) throws IOException {
        String line = null;
        StringBuilder result = new StringBuilder();
        long begin = System.currentTimeMillis();

        int ret = 0;
        char[] cbuf = new char[1024];
        while((ret = reader.read(cbuf)) != -1) {
            if(System.currentTimeMillis() - begin > interval) {
                throw new RuntimeException("read timeout");
            }
            if (result.length() > 1024 * 32) {
                throw new RuntimeException("read large");
            }
            result.append(cbuf, 0, ret);
        }
        return result.toString();
    }

    public static String readStream(BufferedReader reader, long interval) throws TimeoutException {
        String line = null;
        StringBuilder result = new StringBuilder();
        long begin = System.currentTimeMillis();
        try {
            while ((line = reader.readLine()) != null) {
                if(System.currentTimeMillis() - begin > interval) {
                    throw new TimeoutException("read timeout");
                }
                if (result.length() > 1024 * 256) {
                    throw new RuntimeException("read large");
                }
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            if (result.length() > 128) {
                result = result.delete(128, result.length());
            }
            log.error("输入流异常！", e);
        } finally {
            StreamUtils.close(reader);
        }
        return result.toString();
    }

    public static void writeStream(BufferedWriter writer, String input) {
        final String[] split = input.trim().split("\n");
        try {
            for (String s : split) {
                writer.write(s);
                writer.write('\n');
                writer.flush();
            }
        } catch (IOException e) {
            log.error("输出流异常！", e);
        } finally {
            close(writer);
        }
    }

}
