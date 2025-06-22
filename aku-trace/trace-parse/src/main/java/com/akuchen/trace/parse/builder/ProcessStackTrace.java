package com.akuchen.trace.parse.builder;

import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.akuchen.trace.parse.utils.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 堆栈日志处理
 */
@Slf4j
public class ProcessStackTrace {

    /**
     * 过滤出错误的堆栈日志
     *
     * @param lines
     * @return
     */
    public static Map<String, List<DebuggerDTO>> filterErrorLog(List<String> lines) {
        Map<String, List<DebuggerDTO>> map = new HashMap<>();
        for (String line : lines) {
            List<String> errorStacks = regexAkuStackLog(line);
            if (CollectionUtils.isEmpty(errorStacks)) {
                continue;
            }
            List<DebuggerDTO> debuggerDTOS = errorStacks.stream().map(ProcessStackTrace::generateDebuggerDTO).filter(Objects::nonNull).collect(Collectors.toList());
            //只保留1条
            if (debuggerDTOS.size() > 1) {
                debuggerDTOS = debuggerDTOS.subList(0, 1);
            }
            map.put(regexAkuErrorLog(line), debuggerDTOS);
        }
        return map;

    }

    /**
     * 异常的原因
     *
     * @param log
     * @return
     */
    private static String regexAkuErrorLog(String log) {
        //没有太好的方法扣出来,先简化log长度 控制在1000个字符以内
        if (log.length() > 1000) {
            log = log.substring(0, 1000);
        }
        return log;

    }

    /**
     * 错误的业务堆栈日志  也可称为有效的堆栈日志  只获取最前面com.akuchen 6条记录 后面再过滤 最多保留3条
     *
     * @param text
     * @return
     */
    public static List<String> regexAkuStackLog(String text) {
        String keyword = "at com.akuchen";
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        Pattern pattern = Pattern.compile(keyword);
        for (String line : lines) {
            if (pattern.matcher(line).find() && result.size() < 6) {
                //处理这种情况 at com.akuchen.service.orderservice.service.OrderItemService.lambda$deliverBatch$0(OrderItemService.java:201)
                if(line.indexOf("lambda$")>-1){
                    line=line.replaceAll("lambda\\$","");
                    line=line.replaceAll("\\$\\d+","");
                }
                result.add(line);
            }
        }
        return result;
    }

    private static DebuggerDTO generateDebuggerDTO(String text) {
        try {
            int punctuation = text.lastIndexOf(".", text.lastIndexOf(".") - 1);
            String className = text.substring(text.indexOf("at ") + 3, punctuation);
            String methodName = text.substring(punctuation + 1, text.indexOf("("));
            String classLineString = text.substring(text.indexOf(".java:") + 6, text.indexOf(")"));
            Integer classLine=Integer.valueOf(classLineString);
            //1通过git的方式 获取代码

            //2反射和本地文件的方式 获取代码
            String codeSource = ClassUtils.getCodeLine( null,className, Arrays.asList(classLine));

            DebuggerDTO methodIndexAndMethodLineDebugger = ClassUtils.getMethodIndexAndMethodLine(null,className, classLine);
            if(Objects.isNull(methodIndexAndMethodLineDebugger)){
                return null;
            }

            return DebuggerDTO.builder()
                    .className(className)
                    .methodName(methodName)
                    .classLine(classLine)
                    .methodIndex(methodIndexAndMethodLineDebugger.getMethodIndex())
                    .methodLine(methodIndexAndMethodLineDebugger.getMethodLine())
                    .codeSource(codeSource)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


}
