package com.linbo.feature.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ListUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author xbockx
 * @Date 2/12/2023
 */
@Slf4j
public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    private List<Map<Integer, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private ObjectMapper objectMapper = new ObjectMapper();
    private Set<String> headSet;

    @SneakyThrows
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
//        log.info("解析到一条数据:{}", objectMapper.writeValueAsString(data));
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @SneakyThrows
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headSet = new HashSet<>();
        for (Map.Entry<Integer, ReadCellData<?>> readCellDataEntry : headMap.entrySet()) {
            final ReadCellData<?> value = readCellDataEntry.getValue();
            headSet.add(value.getStringValue());
        }
        log.info("解析到一条头数据:{}", objectMapper.writeValueAsString(headSet));
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！");
    }

    public Set<String> getHeadSet() {
        return this.headSet;
    }
}
