package com.linbo.feature.window.service;

import com.linbo.feature.entity.config.ProjectConfigure;

import javax.swing.*;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Description
 * @Author xbockx
 * @Date 2/12/2023
 */
public class BuildService extends AbstractService{

    //当前项目的路径和项目名称
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 更新并保存新的设置
     *
     * @param configure 新的设置
     */
    public void updateAndSaveConfigure(ProjectConfigure configure) {
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(Paths.get(path + "/.config")))) {
            stream.writeObject(configure);
            stream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
