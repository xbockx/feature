package com.linbo.feature.common;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class Loadfont {
    public static Font loadFont(String fontFileName, int fontStyle, float fontSize)  //第一个参数是外部字体名，第二个是字体大小
    {
        try {
            File file = new File(fontFileName);
            FileInputStream aixing = new FileInputStream(file);
            Font dynamicFont = Font.createFont(fontStyle, aixing);
            Font dynamicFontPt = dynamicFont.deriveFont(fontSize);
            aixing.close();
            return dynamicFontPt;
        } catch (Exception e)//异常处理
        {
            e.printStackTrace();
            return new Font("宋体", Font.PLAIN, 14);
        }
    }

//    public static java.awt.Font Font() {
//        String root = System.getProperty("user.dir");//项目根目录路径
//        Font font = Loadfont.loadFont(root + "/data/PRISTINA.ttf", 18f);//调用
//        return font;//返回字体
//    }
//
//    public static java.awt.Font Font2() {
//        String root = System.getProperty("user.dir");//项目根目录路径
//        Font font = Loadfont.loadFont(root + "/data/XXXX.ttf", 18f);
//        return font;//返回字体
//    }

    public static Font getOPlusSans3(int fontStyle, int fontSize) {
        String root = System.getProperty("user.dir");//项目根目录路径
        System.out.println(root);
        Font font = Loadfont.loadFont(root + "/files/OPlusSans3-Regular.ttf", fontStyle, fontSize);
        return font;//返回字体
    }
}
