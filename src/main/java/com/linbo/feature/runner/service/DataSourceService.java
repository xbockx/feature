package com.linbo.feature.runner.service;

import com.linbo.feature.runner.domain.Record;

import java.util.List;

/**
 * @Description
 * @Author xbockx
 * @Date 12/19/2022
 */
public interface DataSourceService {

    List<Record> readFromExcel(String path);

}
