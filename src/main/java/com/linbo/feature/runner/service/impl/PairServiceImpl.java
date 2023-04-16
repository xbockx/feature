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

    private List<Result> tmp;

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
        this.tmp = sorted;
    }

    private void reset() {
        this.sorted = this.tmp;
        this.res.clear();
    }

    public void processAll() {
        diffBefore();
        reset();
        diffReplace();
        reset();
        simple();
    }

    /**
     * 第一个全部通过
     * 比较该节点前面所有编译失败
     * diff size == 1
     */
    public void diffBefore() {
        for (int i = 0; i < sorted.size() - 1; i++) {
            List<Result> firstList = new ArrayList<>();
            Result second = null;
            while (i < sorted.size() - 1 && sorted.get(i).getSource().getStudentId().equals(sorted.get(i + 1).getSource().getStudentId())) {
                // first == null &&  compile fail
                Result curr = sorted.get(i);
                Result next = sorted.get(i + 1);
                if (second == null && curr.getCompileStatus().equals(ResultCode.COMPILE_FAIL.value)) {
                    firstList.add(curr);
                    // case: f f f t
                    if (next.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value) && next.getPassTestNum() == next.getTestCount()) {
                        second = next;
                    }
                }
                if (curr.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value) && next.getCompileStatus().equals(ResultCode.COMPILE_SUCCESS.value)) {
                    // case: f f f _ _ t
                    if (next.getPassTestNum() == next.getTestCount()) {
                        second = next;
                    }
                }

                if (firstList.size() > 0 && second != null) {
                    for (Result first : firstList) {
                        if (first.getSource().getCode() == null || second.getSource().getCode() == null) {
                            continue;
                        }
                        Patch<String> diffRes = DiffUtils.diff(first.getSource().getCode(), second.getSource().getCode(), null);
                        final List<AbstractDelta<String>> deltas = diffRes.getDeltas();
                        if (deltas.size() == 1) {
                            final AbstractDelta<String> delta = deltas.get(0);
                            if (delta.getSource().getLines().size() <= 3 ||  delta.getTarget().getLines().size() <= 3) {
                                ResultPair pair = new ResultPair(first, second);
                                res.add(pair);
                            }
                        }
                    }
                    second = null;
                }
                i++;
            }
            firstList.clear();
        }
        final List<ExportPair> collect = res.stream().map(r -> new ExportPair(r.lastError.getSource().getCode(), r.firstPass.getSource().getCode())).collect(Collectors.toList());
        EasyExcel.write(Application.config.getExportPath() + "/pair/" + Application.config.getName() + "_全部通过之前所有错误DIFF为1.xlsx", ExportPair.class)
                .sheet()
                .doWrite(collect);
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
                i++;
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
                    if (first.getSource().getCode() == null || second.getSource().getCode() == null) {
                        continue;
                    }
                    Patch<String> diffRes = DiffUtils.diff(first.getSource().getCode(), second.getSource().getCode(), null);
                    final List<AbstractDelta<String>> deltas = diffRes.getDeltas();
                    for (int j = 0; j < deltas.size(); j++) {
                        AbstractDelta<String> delta = deltas.get(j);
                        if (delta.getSource().getLines().size() > 3 ||  delta.getTarget().getLines().size() > 3) {
                            break;
                        }
                        if (j == deltas.size() - 1) {
                            final List<String> targetSplit = Arrays.stream(second.getSource().getCode().split("\n")).collect(Collectors.toList());
                            if (delta.getType().equals(DeltaType.DELETE)) {
                                for (int k = delta.getSource().getLines().size() - 1; k >= 0 ; k--) {
                                    targetSplit.remove(delta.getTarget().getPosition());
                                }
                            } else if (delta.getType().equals(DeltaType.CHANGE)) {
                                for (int k = 0; k < delta.getSource().getLines().size(); k++) {
                                    targetSplit.set(delta.getTarget().getPosition() + k, delta.getSource().getLines().get(k));
                                }
                            } else if (delta.getType().equals(DeltaType.INSERT)) {
                                for (int k = 0; k < delta.getSource().getLines().size(); k++) {
                                    targetSplit.add(delta.getTarget().getPosition() + k, delta.getSource().getLines().get(k));
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
            }
        }
        final List<ExportPair> collect = res.stream().map(r -> new ExportPair(r.lastError.getSource().getCode(), r.firstPass.getSource().getCode())).collect(Collectors.toList());
        EasyExcel.write(Application.config.getExportPath() + "/pair/" + Application.config.getName() + "_全部通过替换最后错误.xlsx", ExportPair.class)
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
        EasyExcel.write(Application.config.getExportPath() + "/pair/" + Application.config.getName() + "_最后语法第一部分.xlsx", ExportPair.class)
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
