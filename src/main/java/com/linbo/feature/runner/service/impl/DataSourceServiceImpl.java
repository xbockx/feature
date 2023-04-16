package com.linbo.feature.runner.service.impl;

import com.linbo.feature.runner.domain.Record;
import com.linbo.feature.runner.service.DataSourceService;

import java.util.List;

/**
 * @Description
 * @Author xbockx
 * @Date 12/19/2022
 */
public class DataSourceServiceImpl implements DataSourceService {
    @Override
    public List<Record> readFromExcel(String path) {
//        EasyExcel.read(fileName, DemoData.class, new DemoDataListener()).sheet().doRead();
        return null;
    }
}
