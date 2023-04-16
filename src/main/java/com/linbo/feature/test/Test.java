package com.linbo.feature.test;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.linbo.feature.entity.config.Execute;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author xbockx
 * @Date 2/16/2023
 */
public class Test {

    public static void main(String[] args) {
        List<Execute> list = new ArrayList<>(50000);
        List<Execute> res = new ArrayList<>(50000);
        EasyExcel.read("C:\\Users\\imlin\\Desktop\\tb_execute_包含报错信息.xlsx", Execute.class, new ReadListener<Execute>() {
            @Override
            public void invoke(Execute execute, AnalysisContext analysisContext) {
                list.add(execute);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }
        }).sheet().doRead();
        Pattern pattern = Pattern.compile("20[0-9]{12}.c:[0-9]+:[0-9]+: error: ");
        for (int i = 0; i < list.size(); i++) {
            String[] lines = list.get(i).getCompileResult().split("\n");
            for (int j = 0; j < lines.length; j++) {
                String line = lines[j];
                final Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    list.get(i).setCompileResult(line.substring(matcher.end()));
                    res.add(list.get(i));
                    break;
                }
            }
        }
//        System.out.println(res);
        EasyExcel.write("D://test.xlsx").sheet().head(Execute.class).doWrite(res);
    }

}
