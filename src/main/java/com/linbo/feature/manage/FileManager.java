package com.linbo.feature.manage;

import com.linbo.feature.entity.config.CreateProjectConfigure;
import com.linbo.feature.entity.config.ProjectConfigure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileManager {
    public static boolean createProject(CreateProjectConfigure configure){
        File dir = new File(configure.getPath() + "/" +configure.getName());
        if(dir.exists() || dir.mkdirs()) {
            File src = new File(dir.getAbsolutePath() + "/workspace/");
            createProjectConfigure(configure);
            ProjectManager.createProject(configure.getName(), configure.getPath());
            if(!src.mkdir()) return false;
            if(configure.hasDefaultCode()) {
                File defaultCodeFile = new File(dir.getAbsolutePath() + "/src/Main.java");
                try(FileWriter writer = new FileWriter(defaultCodeFile)) {
                    writer.write(defaultMainCode());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static void createProjectConfigure(CreateProjectConfigure configure){
        ProjectConfigure projectConfigure = new ProjectConfigure(new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
        try(ObjectOutputStream writer = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(configure.getPath() + "/" + configure.getName() + "/.config")))) {
            writer.writeObject(projectConfigure);
            writer.flush();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public static void deleteProject(String name, String path) {
        Queue<File> deleteQueue = new LinkedList<>();   //bfs算法删除项目目录下全部文件
        deleteQueue.add(new File(path));
        while (!deleteQueue.isEmpty()) {
            File file = deleteQueue.poll();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    if (!file.delete()) doNothing();
                } else {
                    deleteQueue.addAll(Arrays.asList(files));
                    deleteQueue.add(file);
                }
            } else {
                if (!file.delete()) doNothing();
            }
        }
        ProjectManager.deleteProject(name);
    }

    public static String defaultCode(String className, String packageName){
        return  "package " + packageName + ";\n" +
                "\n" +
                "public class "+className+" {\n" +
                "\n" +
                "}";
    }

    private static String defaultMainCode(){
        return "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello World!\");\n" +
                "    }\n" +
                "}";
    }

    private static void doNothing(){}
}
