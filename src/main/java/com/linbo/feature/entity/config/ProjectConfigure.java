package com.linbo.feature.entity.config;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 项目配置实体类
 */
@Data
public class ProjectConfigure implements Serializable {
    private final Map<String, String> props;
    private final List<String> filenames;
    private final Map<String, Map<String, Integer>> executeKeyMap;
    private final Map<String, Map<String, Integer>> testcaseKeyMap;

    public ProjectConfigure(Map<String, String> props,
                            List<String> filenames,
                            Map<String, Map<String, Integer>> executeKeyMap,
                            Map<String, Map<String, Integer>> testcaseKeyMap) {
        this.props = props;
        this.filenames = filenames;
        this.executeKeyMap = executeKeyMap;
        this.testcaseKeyMap = testcaseKeyMap;
    }

}
