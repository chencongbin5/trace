package com.akuchen.trace.parse.utils;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import com.akuchen.trace.parse.builder.MockClassBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ValUtils {



    private static Integer METHOD_INDEX=0;

    /**
     * 特殊字符去掉, 会影响解析
     * @param messages
     * @return
     */
    public static List<String> replace(List<String> messages){
        return messages.stream().map(line->line.replaceAll("\\\\t|\\\\r|\\\\n","")).collect(Collectors.toList());
    }

    /**
     * 更新日志的简化类为全名
     * @param messages
     * @return
     */
    public static List<String> modifyLogClassName(List<String> messages){
        Reflections reflections = new Reflections("com.akuchen.service.calculation.remote");
        Set<Class<?>> clazz = reflections.getTypesAnnotatedWith(Service.class);
        List<String> classNameList = clazz.stream().flatMap(zlass -> Arrays.stream(zlass.getDeclaredFields()).map(field -> field.getType().getName())).collect(Collectors.toList());
        classNameList.add("com.akuchen.calculation.api.api.CalcServiceApi");
        classNameList.add("com.akuchen.midend.kms.api.client.KeyService");
        //修改日志的类名为全名
        List<String> realMessages=new ArrayList<>();
        realMessages.addAll(replaceClassName(messages,classNameList,"[RPC-PROVIDER]"));
        realMessages.addAll(replaceClassName(messages,classNameList,"[RPC]"));
        return realMessages;
    }

    private static List<String> replaceClassName(List<String> messages,List<String> classNameList,String prefix){
        return messages.stream().filter(line->line.startsWith(prefix)).map(line->{
            String classAndMethod = line.substring(line.indexOf(prefix) + prefix.length()+1, line.indexOf("]【request"));
            String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
            String filterClassName = classNameList.stream().filter(name -> name.endsWith(className)).findFirst().orElse(null);
            System.out.println(filterClassName);
            return line.replaceFirst(className,filterClassName);
        }).collect(Collectors.toList());
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        return random.ints(48, 91) // 91 is exclusive, so the max is 90
                .filter(i -> (i <= 57 || i >= 65))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String getLargeText(String text,String outFilePath){
        if(text.length()>20000){
            //1文件名称
            String fileName = "json_"+ValUtils.generateRandomString(6)+".log";
            String filePath = outFilePath + "/" + fileName;
            try {
                FileUtils.write(new File(filePath), text, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //指向文件
            return "FileUtils.readFileToString(new File(\""+filePath+"\"))";
        }
        return "\"" + MockClassBuilder.valAdd(text) + "\"";
    }

    public static Integer valAdd() {
        return METHOD_INDEX++;
    }
    public static void valReset(){
        METHOD_INDEX=0;
    }

}
