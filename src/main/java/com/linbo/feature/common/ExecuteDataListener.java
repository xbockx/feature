package com.linbo.feature.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linbo.feature.entity.ExecuteEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Description
 * @Author xbockx
 * @Date 2/13/2023
 */
@Slf4j
public class ExecuteDataListener extends AnalysisEventListener<Map<Integer, String>> {

    private List<Map<Integer, String>> res = new ArrayList<>();
    private Map<String, Integer> headKeyMap;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void invoke(Map<Integer, String> executeEntity, AnalysisContext analysisContext) {
//        try {
//            log.info("解析到一条数据:{}", objectMapper.writeValueAsString(executeEntity));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        res.add(executeEntity);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headKeyMap = new HashMap<>();
        for (Map.Entry<Integer, ReadCellData<?>> readCellDataEntry : headMap.entrySet()) {
            headKeyMap.put(readCellDataEntry.getValue().getStringValue(), readCellDataEntry.getKey());
        }
//        try {
//            log.info("解析到一条数据:{}", objectMapper.writeValueAsString(headKeyMap));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
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
