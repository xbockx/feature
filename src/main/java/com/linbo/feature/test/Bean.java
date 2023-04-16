package com.linbo.feature.test;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Description
 * @Author xbockx
 * @Date 2/26/2023
 */
@Data
public class Bean {

    @ExcelProperty(index = 0)
    private String eng;
    @ExcelProperty(index = 1)
    private String chn;
    @ExcelProperty(index = 2)
    private String type;
    @ExcelProperty(index = 3)
    private String exp;

}
