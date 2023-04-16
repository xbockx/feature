package com.linbo.feature.test;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.StringUtils;
import com.linbo.feature.entity.config.Execute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description excel union
 * @Author xbockx
 * @Date 2/26/2023
 */
public class Test2 {

    public static void main(String[] args) {

        List<Bean> oldList = new ArrayList<>(1000);
        List<Bean> newList = new ArrayList<>(1000);

        List<Bean> res = new ArrayList<>(1000);

        Map<String, Bean> oldMap = new HashMap<>();

        EasyExcel.read("C:\\Users\\imlin\\Desktop\\dict.xlsx", Bean.class, new ReadListener<Bean>() {
            @Override
            public void invoke(Bean bean, AnalysisContext analysisContext) {
                oldList.add(bean);
                oldMap.put(bean.getEng(), bean);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }
        }).sheet(1).doRead();

        EasyExcel.read("C:\\Users\\imlin\\Desktop\\all_replace_word_count.xlsx", Bean.class, new ReadListener<Bean>() {
            @Override
            public void invoke(Bean bean, AnalysisContext analysisContext) {
                newList.add(bean);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }
        }).sheet(0).doRead();

        for (Bean bean : newList) {
            final Bean old = oldMap.get(bean.getEng());
            if (old == null) {
                continue;
            }
            if (StringUtils.isEmpty(bean.getChn())) {
                bean.setChn(old.getChn());
            }
            if (StringUtils.isEmpty(bean.getType())) {
                bean.setType(old.getType());
            }
            if (StringUtils.isEmpty(bean.getExp())) {
                bean.setExp(old.getExp());
            }
        }

        EasyExcel.write("D://RES11111.xlsx").sheet().head(Bean.class).doWrite(newList);

    }

}
