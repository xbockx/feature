package com.linbo.feature.runner.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.linbo.feature.test.Cat;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description
 * @Author xbockx
 * @Date 3/27/2023
 */
public class CodeUtils {

    public static Pattern printfPattern = Pattern.compile(".*printf *\\(.*\\)");
    public static Pattern scanfPattern = Pattern.compile(".*scanf *\\(.*\\)");
//    public static Set<Character> pSet = new HashSet<>(Arrays.asList('d', 'i', 'o', 'u', 'x', 'X', 'f', 'e', 'E', 'g', 'G', 'c', 's', 'p', 'n'));
    public static Pattern placeholder = Pattern.compile("%[0-9]*\\.*[0-9]*(d|i|o|u|x|X|f|e|E|g|G|c|s|p|n|lf)");
    public static String SEP = " ";

    public static String format(String code) {
        String[] lines = preFormat(code);
        if (lines.length == 1) {
            throw new RuntimeException("Format Exception");
        }
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.equals("")) {
                continue;
            }
            if (printfPattern.matcher(line).find()) {
//                System.out.println("[pre]: " + line);
                String c = conv(line, "printf");
                builder.append(c).append('\n');
//                System.out.println("[post]: " + c);
            } else if (scanfPattern.matcher(line).find()) {
                String c = conv(line, "scanf");
                builder.append(c).append('\n');
            } else {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private static String[] preFormat(String code) {
        code = code.trim();
        code = code.replaceAll("\\);", "\\);\n");
        String[] lines = code.split("\n");
        return lines;
    }


    private static String conv(String line, String keyword) {
        int[] matchIndex = findMatch(line.indexOf(keyword) + keyword.length(), line, '(', ')');
        int start = matchIndex[0];
        int end = matchIndex[1];
        String pre = line.substring(0, start + 1);
        String post = line.substring(end);

        String content = line.substring(start + 1, end).trim();
        if (!content.startsWith("\"")) {
            throw new RuntimeException("Parameter Exception");
        }
        int[] quotaIndex = findMatch(0, content, '"', '"');
        int quotaStartIndex = quotaIndex[0];
        int quotaEndIndex = quotaIndex[1];

        // exp
        String quotaStr = content.substring(quotaStartIndex, quotaEndIndex + 1).trim();
        // params
        String paramStr = content.substring(quotaEndIndex + 1);
        int paramSize = 0;
        content = "";

        // params content
        String[] split = paramStr.split(",");
        for (int i = 0; i < split.length; i++) {
            String s = split[i].trim();
            s = s.trim();
            if (s.equals("")) {
                continue;
            }
            if (!content.equals("")) {
                content += ", ";
            }
            content += s;
            paramSize++;
        }

        // exp content
        content = content.isEmpty() ? expParse(quotaStr, paramSize, SEP) : expParse(quotaStr, paramSize, SEP) + ", " + content;
        return pre + content + post;
    }

    private static String expParse(String exp, int paramSize, String separator) {
        String parsed = "";
        exp = exp.substring(1, exp.length() - 1);
//        int p = 0;
//        for (int i = 0; i < exp.length(); i++) {
//            if (exp.charAt(i) == '%') {
//                if (i + 1 < exp.length()) {
//                    if (pSet.contains(exp.charAt(i + 1))) {
//                        if (!parsed.equals("")) {
//                            parsed += " ";
//                        }
//                        parsed += ("%" + exp.charAt(i + 1));
//                        p++;
//                    }
//                }
//                i++;
//            }
//        }
        Matcher matcher = placeholder.matcher(exp);
        int p = 0;
        while(matcher.find()) {
            String group = matcher.group();
            if (!parsed.equals("")) {
                parsed += separator;
            }
            parsed += group;
            p++;
        }
        if (p != paramSize) {
            throw new RuntimeException("Number of parameters does not match");
        }
        return "\"" + parsed + "\"";
    }

    private static int[] findMatch(int start, String line, char left, char right) {
        int end = start;
        int flag = 0;
        if (left == right) {
            for (int i = start; i < line.length(); i++) {
                if (line.charAt(i) == left) {
                    if (i - 1 >= start && line.charAt(i - 1) == '\\') {
                        continue;
                    }
                    if (flag == 0) {
                        flag = 1;
                        start = i;
                    } else {
                        end = i;
                    }
                }
            }
        } else {
            for (int i = start; i < line.length(); i++) {
                if (start != end) {
                    break;
                }
                if (line.charAt(i) == left) {
                    if (flag == 0) {
                        start = i;
                    } else {
                        flag++;
                    }
                } else if (line.charAt(i) == right) {
                    if (flag == 0) {
                        end = i;
                    } else {
                        flag--;
                    }
                }
            }
        }
        return new int[]{start, end};
    }

    public static void main(String[] args) {

//        testFromFile();
        testSingle();

    }

    public static void testFromFile() {
        List<Cat> list = new ArrayList<>(1000);
        EasyExcel.read("C:\\Users\\imlin\\Desktop\\cat.xlsx", Cat.class, new ReadListener<Cat>() {
            @Override
            public void invoke(Cat cat, AnalysisContext analysisContext) {
                list.add(cat);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {

            }
        }).sheet(2).doRead();

        CodeUtils obj = new CodeUtils();
        for (int i = 0; i < list.size(); i++) {
            if (i == 19) {
                System.out.println("");
            }
//            System.out.println("========" + i + "==============");
            Cat cat = list.get(i);
            try {
                final String standardize = obj.format(cat.getCode());
                cat.setCodeReplace(standardize);
            } catch (Exception e) {
                cat.setCodeReplace("[exception]: " + e);
            }
        }
        EasyExcel.write("./test.xlsx").sheet().head(Cat.class).doWrite(list);
    }

    public static void testSingle() {
        String str = "#include <stdio.h>\n" +
                "\n" +
                "int main()\n" +
                "{\n" +
                "    int a,b,c;\n" +
                "\tscanf(\"%d, %d\",&a,&b);\n" +
                "\tc=a;\n" +
                "\ta=b;\n" +
                "\tb=c;\n" +
                "\tprintf(\"a=%.5d\\n\",a);printf(\"b=%d\\n\",b);\n" +
                "    return 0;\n" +
                "}";
        final String format = CodeUtils.format(str);
        System.out.println(format);
    }

}
