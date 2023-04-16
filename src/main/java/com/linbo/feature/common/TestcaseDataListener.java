package com.linbo.feature.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.linbo.feature.entity.ExecuteEntity;
import com.linbo.feature.entity.TestcaseEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Description
 * @Author xbockx
 * @Date 2/13/2023
 */
@Slf4j
public class TestcaseDataListener extends AnalysisEventListener<Map<Integer, String>> {
    private List<Map<Integer, String>> res = new ArrayList<>();
    private Map<String, Integer> headKeyMap;

    @Override
    public void invoke(Map<Integer, String> testcaseEntity, AnalysisContext analysisContext) {
//        log.info("解析到一条数据:{}", JSON.toJSONString(data));
        res.add(testcaseEntity);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headKeyMap = new HashMap<>();
        for (Map.Entry<Integer, ReadCellData<?>> readCellDataEntry : headMap.entrySet()) {
            headKeyMap.put(readCellDataEntry.getValue().getStringValue(), readCellDataEntry.getKey());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

    public List<Map<Integer, String>> getRes() {
        return this.res;
    }

    public Map<String, Integer> getHeadSet() {
        return this.headKeyMap;
    }
}
