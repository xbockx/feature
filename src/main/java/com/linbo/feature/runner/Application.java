package com.linbo.feature.runner;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linbo.feature.runner.config.AppConfig;
import com.linbo.feature.runner.config.BaseConfig;
import com.linbo.feature.runner.core.FileService;
import com.linbo.feature.runner.domain.Record;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.domain.TestCase;
import com.linbo.feature.runner.service.impl.RunServiceImpl;
import com.linbo.feature.runner.util.CodeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author xbockx
 * @Date 10/24/2022
 */
@Slf4j
public class Application {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean START_MULTI_THREAD = false;

    public static BaseConfig config;

    public static void main(String[] args) {

        String configPath = null;
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                configPath = args[0];
            }
        }
        if (configPath == null || configPath.isEmpty()) {
            log.info("parameter error!");
            return;
        }

        config = loadResource(configPath);
        if (config == null) {
            log.info("config error!");
            return;
        }
        log.debug("config: {}", config);

        START_MULTI_THREAD = config.isStartMultiThread();
        //加载Runner Config
        new AppConfig(config.getRootPath());

        // 读取执行记录
        List<Record> records = new ArrayList<>(3000);
        EasyExcel.read(config.getExecutePath(), Record.class, new ReadListener<Record>() {
            @Override
            public void invoke(Record record, AnalysisContext analysisContext) {
                records.add(record);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            }
        }).sheet(0).doRead();

        // 测试用例
        List<TestCase> testCases = new ArrayList<>();
        EasyExcel.read(config.getTestcasePath(), TestCase.class, new ReadListener<TestCase>() {
            @Override
            public void invoke(TestCase testCase, AnalysisContext analysisContext) {
                testCases.add(testCase);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            }
        }).sheet(0).doRead();
        final Map<Integer, List<TestCase>> testcaseMap = getTestCases(testCases);

        final FileService fileService = FileService.getInstance();

        // 输出重写
        if (config.getJudgeType().equals("number")) {
            for (int i = 0; i < records.size(); i++) {
                try {
                    records.get(i).setCode(CodeUtils.format(records.get(i).getCode()));
                } catch (Exception e) {
                    log.debug("ex: {}", e.getMessage());
                }
            }
        }

        // 写入文件
        try {
            fileService.writeCode(records);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("文件写入完成");

        // 编译运行
        String perPath = config.getExportPath() + "persist/" + config.getName() + ".json";
        List<Result> exec = null;
        if (Files.exists(Paths.get(perPath))) {
            log.info("执行结果已存在");
            try {
                exec = objectMapper.readValue(new File(perPath), new TypeReference<List<Result>>() {});
            } catch (IOException e) {
                exec = new RunServiceImpl().exec(records, testcaseMap);
                e.printStackTrace();
            }
        } else {
            exec = new RunServiceImpl().exec(records, testcaseMap);
            try {
                objectMapper.writeValue(new File(config.getExportPath() + "persist/" + config.getName() + ".json"), exec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!CollectionUtils.isEmpty(exec)) {
            log.info("编译运行完成");
            process(exec);
        }

        // 结果写入
        String pathName = null;
        if (START_MULTI_THREAD) {
            pathName = config.getExportPath() + config.getName() + "_multi.xlsx";
        } else {
            pathName = config.getExportPath() + config.getName() + "_single.xlsx";
        }
        EasyExcel.write(pathName, Result.class).sheet().doWrite(exec);
        log.info("结果写入完成");
    }

    private static BaseConfig loadResource(String path) {
        BaseConfig config = new BaseConfig();
        try(InputStream is = new FileInputStream(path)) {
            Yaml yaml = new Yaml(new Constructor(Map.class));
            Map<String, Object> map = yaml.load(is);
            final String name = String.valueOf(map.get("name"));
            final Boolean startMultiThread = Boolean.valueOf(String.valueOf(map.get("multi-thread")));
            final String judgeType = String.valueOf(map.get("judge-type"));
            final String judgeCase = String.valueOf(map.get("judge-case"));
            final Map<String, String> subMap = (Map<String, String>) map.get("path");
            final String execute = subMap.get("execute");
            final String testcase = subMap.get("testcase");
            final String root = subMap.get("root");
            final String export = subMap.get("export");
            config.setExecutePath(execute);
            config.setTestcasePath(testcase);
            config.setRootPath(root);
            config.setExportPath(export);
            config.setName(name);
            config.setJudgeType(judgeType);
            config.setStartMultiThread(startMultiThread);
            config.setJudgeCase(judgeCase);

            if (Files.notExists(Paths.get(config.getRootPath()))) {
                new File(config.getRootPath()).mkdirs();
            }
            if (Files.notExists(Paths.get(config.getExportPath()))) {
                new File(config.getExportPath()).mkdirs();
                if (Files.notExists(Paths.get(config.getExportPath() + "pair"))) {
                    new File(config.getExportPath() + "pair").mkdirs();
                }
                if (Files.notExists(Paths.get(config.getExportPath() + "persist"))) {
                    new File(config.getExportPath() + "persist").mkdirs();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static void process(List<Result> exec) {
        // sort by studentId, id(time)
        final List<Result> sorted = exec.stream().sorted((o1, o2) -> {
            if (o1.getSource().getStudentId().equals(o2.getSource().getStudentId())) {
                return o1.getSource().getId() - o2.getSource().getId();
            }
            return o1.getSource().getStudentId().compareTo(o2.getSource().getStudentId());
        }).collect(Collectors.toList());

        List<ResultPair> res = new ArrayList<>();
        for (int i = 0; i < sorted.size() - 1; i++) {
            Result first = null;
            Result second = null;
            while (i < sorted.size() - 1 && sorted.get(i).getSource().getStudentId().equals(sorted.get(i + 1).getSource().getStudentId())) {
                // first == null &&  compile fail
                Result curr = sorted.get(i);
                Result next = sorted.get(i + 1);
                if (curr.getCompileStatus().equals(ResultCode.COMPILE_FAIL.value) && next.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value)) {
                    // case: f f f t
                    first = curr;
                    if (next.getPassTestNum() > 0) {
                        second = next;
                    }
                } else if (curr.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value) && next.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value)) {
                    // case: f f f _ _ t
                    if (first != null && next.getPassTestNum() > 0) {
                        second = next;
                    }
                }

                if (first != null && second != null) {
                    ResultPair pair = new ResultPair(first, second);
                    res.add(pair);
                    first = null;
                    second = null;
                }
                i++;
            }
        }
        final List<ExportPair> collect = res.stream().map(r -> new ExportPair(r.lastError.getSource().getCode(), r.firstPass.getSource().getCode())).collect(Collectors.toList());
        EasyExcel.write(config.getExportPath() + "/pair/" + config.getName() + ".xlsx", ExportPair.class)
                .sheet()
                .doWrite(collect);
    }

    @Data
    private static class ExportPair {
        String lastError;
        String firstPass;
        ExportPair(String lastError, String firstPass) {
            this.lastError = lastError;
            this.firstPass = firstPass;
        }
    }

    private static class ResultPair {
        // 最后一个编译错误
        Result lastError;

        // 第一个通过部分测试用例
        Result firstPass;

        ResultPair(Result lastError, Result firstPass) {
            this.lastError = lastError;
            this.firstPass = firstPass;
        }
    }

    public static Map<Integer, List<TestCase>> getTestCases(List<TestCase> testCases) {
        Map<Integer, List<TestCase>> map = new ConcurrentHashMap<>();
        for (TestCase testCase : testCases) {
            log.debug("读取到一条TestCase数据{}", testCase.getId());
            List<TestCase> list = map.get(testCase.getPid());
            if (list != null) {
                list.add(testCase);
            } else {
                list = new ArrayList<>();
                list.add(testCase);
                map.put(testCase.getPid(), list);
            }
        }
        return map;
    }

}
