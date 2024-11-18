package com.akuchen.trace.parse.utils;

import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 类工具类
 */
public class ClassUtils {

    /**
     * 获取指定类的指定行数的代码
     * <p>
     * 这里有问题 本地可能找不到文件,  它可能在sdk里头
     *
     * @param className  类名
     * @param lineNumber 行数
     * @return
     * @throws IOException
     */
    public static String getCodeLine(Class cla, String className, List<Integer> lineNumber) throws IOException {
        List<String> codeLines = getCodeLines(cla, className, lineNumber);
        // 如果指定行数超出文件的实际行数，或文件为空，返回空字符串
        if (CollectionUtils.isEmpty(codeLines)) {
            return "";
        }
        return codeLines.stream().collect(Collectors.joining("\n"));
    }


    public static List<String> getCodeLines(Class cla, String className, List<Integer> lineNumber) throws IOException {
        String filePath = findManagerPath(cla) + "/src/main/java/" + className.replaceAll("\\.", "/") + ".java";
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String codeLine;
            int currentLine = 1;

            while ((codeLine = br.readLine()) != null) {
                if (lineNumber.contains(currentLine)) {
                    result.add(codeLine);
                }
                currentLine++;
            }
        }
        return result;
    }

    /**
     * 获取目标行的上下行记录
     *
     * @param line 目标行
     * @param up   上几行
     * @param down 下几行
     * @return
     */
    public static List<Integer> calMoreLines(Integer line, Integer up, Integer down) {
        List<Integer> arr = new ArrayList<>();
        for (Integer i = 1; i <= up; i++) {
            arr.add(line - i);
        }
        arr.add(line);
        for (Integer i = 1; i <= down; i++) {
            arr.add(line + i);
        }
        return arr;
    }

    public static DebuggerDTO getMethodIndexAndMethodLine(Class cla, String className, Integer lineNumber) throws FileNotFoundException {
        String filePath = findManagerPath(cla) + "/src/main/java/" + className.replaceAll("\\.", "/") + ".java";
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File(filePath));
        MethodDeclaration methodDeclaration = findMethodByLine(compilationUnit, lineNumber);
        if (Objects.isNull(methodDeclaration)) {
            return null;
        }
        String methodName = Optional.ofNullable(methodDeclaration).map(MethodDeclaration::getNameAsString).orElse(null);
        //方法开始的行数
        int line = methodDeclaration.getBegin().get().line;
        //根据方法名 获取有几个同名的方法
        List<MethodDeclaration> methodDeclarationList = compilationUnit.findAll(MethodDeclaration.class).stream().filter(method -> method.getNameAsString().equals(methodName)).collect(Collectors.toList());
        //找到方法的index
        Integer methodIndex = 0;
        for (int i = 0; i < methodDeclarationList.size(); i++) {
            if (methodDeclarationList.get(i).getBegin().get().line == line) {
                methodIndex = i;
                break;
            }
        }
        Integer methodLine = lineNumber - line;

        //lineNumber - line 这块代码之间如果有的行 是空行  有的行是注释行// methodline 还要再扣 所以这里需要读取文件信息
        try {
            List<String> codeLines = getCodeLines(cla, className, calMoreLines(lineNumber, methodLine, 0));
            Long count = codeLines.stream()
                    .filter(t -> validateRegular(t, "\\s*//.*") ||  validateRegular(t, "\\s*") || StringUtils.isEmpty(t))
                    .count();
            methodLine = methodLine - count.intValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //1行 改0行
        methodLine = methodLine - 1;

        return DebuggerDTO.builder().methodIndex(methodIndex).methodLine(methodLine).build();

    }


    /**
     * 校验是否符合给定的正则表达式
     *
     * @return
     */
    private static boolean validateRegular(String content, String regular) {
        Pattern pattern = Pattern.compile(regular);
        return pattern.matcher(content).matches();
    }

    private static MethodDeclaration findMethodByLine(Node node, int lineNumber) {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) node;
            if (methodDeclaration.getBegin().isPresent() && methodDeclaration.getEnd().isPresent()) {
                int methodStartLine = methodDeclaration.getBegin().get().line;
                int methodEndLine = methodDeclaration.getEnd().get().line;
                if (lineNumber >= methodStartLine && lineNumber <= methodEndLine) {
                    return methodDeclaration;
                }
            }
        }

        for (Node child : node.getChildNodes()) {
            MethodDeclaration method = findMethodByLine(child, lineNumber);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    private static String findManagerPath(Class cla) {
        if (Objects.isNull(cla)) {
            return System.getProperty("user.dir");
        }
        String codeFile = cla.getProtectionDomain().getCodeSource().getLocation().getPath();
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            //windows   不要最前面的 / 符号
            return codeFile.substring(1, codeFile.indexOf("/target"));
        }
        return codeFile.substring(0, codeFile.indexOf("/target"));

    }


}
