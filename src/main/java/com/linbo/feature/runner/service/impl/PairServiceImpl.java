package com.linbo.feature.runner.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.linbo.feature.runner.Application;
import com.linbo.feature.runner.domain.Result;
import com.linbo.feature.runner.domain.ResultCode;
import com.linbo.feature.runner.service.PairService;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 找代码对
 * @Author xbockx
 * @Date 4/16/2023
 */
public class PairServiceImpl {

    private List<Result> sorted;

    List<ResultPair> res = new ArrayList<>();

    public PairServiceImpl(List<Result> exec) {
        // sort by studentId, id(time)
        final List<Result> sorted = exec.stream().sorted((o1, o2) -> {
            if (o1.getSource().getStudentId().equals(o2.getSource().getStudentId())) {
                return o1.getSource().getId() - o2.getSource().getId();
            }
            return o1.getSource().getStudentId().compareTo(o2.getSource().getStudentId());
        }).collect(Collectors.toList());
        this.sorted = sorted;
    }

    /**
     * diff
     * 第一个通过全部测试用例的正确代码
     * 替换最后一个语法错误的最后一个错误
     */
    public void diffReplace() {
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
                    if (next.getPassTestNum() == next.getTestCount()) {
                        second = next;
                    }
                } else if (curr.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value) && next.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value)) {
                    // case: f f f _ _ t
                    if (first != null && next.getPassTestNum() == next.getTestCount()) {
                        second = next;
                    }
                }

                if (first != null && second != null) {
                    Patch<String> diffRes = DiffUtils.diff(first.getSource().getCode(), second.getSource().getCode(), null);
                    final List<AbstractDelta<String>> deltas = diffRes.getDeltas();
                    for (int j = 0; j < deltas.size(); j++) {
                        AbstractDelta<String> delta = deltas.get(j);
                        if (delta.getSource().getLines().size() > 3 ||  delta.getTarget().getLines().size() > 3) {
                            break;
                        }
                        if (j == deltas.size() - 1) {
                            final List<String> lines = delta.getSource().getLines();
                            final List<Integer> changePosition = delta.getSource().getChangePosition();
                            final List<String> targetSplit = Arrays.stream(second.getSource().getCode().split("\n")).collect(Collectors.toList());
                            if (delta.getType().equals(DeltaType.DELETE)) {
                                for (Integer index : changePosition) {
                                    targetSplit.remove(lines.get(changePosition.get(0)));
                                }
                            } else if (delta.getType().equals(DeltaType.CHANGE)) {
                                for (int k = 0; k < delta.getSource().getLines().size(); k++) {
                                    targetSplit.set(delta.getTarget().getPosition() + k, delta.getSource().getLines().get(k));
                                }
                            } else if (delta.getType().equals(DeltaType.INSERT)) {
                                for (Integer index : changePosition) {
                                    targetSplit.add(lines.get(index));
                                }
                            }
                            StringBuilder builder = new StringBuilder();
                            for (String s : targetSplit) {
                                builder.append(s).append("\n");
                            }
                            first.getSource().setCode(builder.toString());
                            ResultPair pair = new ResultPair(first, second);
                            res.add(pair);
                        }
                    }
                    first = null;
                    second = null;
                }
                i++;
            }
        }
        final List<ExportPair> collect = res.stream().map(r -> new ExportPair(r.lastError.getSource().getCode(), r.firstPass.getSource().getCode())).collect(Collectors.toList());
        EasyExcel.write(Application.config.getExportPath() + "/pair/" + Application.config.getName() + ".xlsx", ExportPair.class)
                .sheet()
                .doWrite(collect);
    }

    /**
     * [最后一个语法错误, 第一个部分通过]
     */
    public void simple() {
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
        EasyExcel.write(Application.config.getExportPath() + "/pair/" + Application.config.getName() + ".xlsx", ExportPair.class)
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

}
