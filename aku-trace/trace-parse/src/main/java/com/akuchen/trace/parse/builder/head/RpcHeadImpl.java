package com.akuchen.trace.parse.builder.head;

import com.akuchen.trace.parse.builder.MockClassBuilder;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.utils.ApolloUtils;
import com.akuchen.trace.parse.utils.ValUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class RpcHeadImpl implements IHead{

    @Override
    public  CodeInfoDTO disposeHead(String log, String type, Integer count,String outFilePath) {
        //获取入口
        List<String> list = new ArrayList<>();
        List<String> methodList=new ArrayList<>();
        List<String> headList = new ArrayList<>();
        try {
            String classAndMethod = log.substring(log.indexOf(type) + (type.length() + 1), log.indexOf("]【request"));
            String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
            String methodName = classAndMethod.substring(classAndMethod.lastIndexOf(".") + 1);
            String requestJson = log.substring(log.indexOf("request=") + 8, log.indexOf("】"));
            String responseJson = log.substring(log.indexOf("response=") + 9, log.indexOf("】[cost"));
            Class<?> aClass = ClassUtils.getClass(className);
            String classNameLowerCase = MockClassBuilder.firstToLowerCase(className.substring(className.lastIndexOf(".") + 1));
            headList.add("@Autowired    private " + className + " " + classNameLowerCase + ";");

            methodList.add(methodName+"_"+ ValUtils.valAdd());
            //匹配的方法 ,可能还是有多个,  多态么
            List<Method> methods = Arrays.stream(aClass.getDeclaredMethods())
                                         .filter(t -> Objects.equals(t.getName(), methodName))
                                         .collect(
                                                 Collectors.toList());
            //匹配找出唯一的这个  先根据返回结果过滤(好像做不到 除非返回了出参type), 再根据入参过滤
            for (int i=0;i<methods.size();i++){
                Method method = methods.get(i);
                //String paramName = Arrays.stream(method.getGenericParameterTypes()).findFirst().get().getTypeName();
                list.add("//"+classAndMethod);//给kucode 准备的定位信息
                list.add("try{");
                //方法名
                //结尾
                list.add("//InvocationMatcher.matchRecordInit();");
                List<String> params = MockClassBuilder.assemblyParam(JSON.parseArray(requestJson), method.getGenericParameterTypes(), classAndMethod,false);
                //list.add(paramName + " param=JSON.parseObject(\"" + valAdd(requestJson) + "\", new TypeReference<" + paramName + ">() {});");
                list.add(" ");
                list.add("//跳过指定字段 Equality.addField(...);");
                Optional.ofNullable(ApolloUtils.getWrhiteListFields()).ifPresent(fields->{
                    fields.forEach(t->{
                        list.add("//Equality.addField(\""+t+"\");");
                    });
                });
                list.add("    ");
                list.add("//when");
                String returnName = method.getGenericReturnType().getTypeName();
                list.add(returnName + " alResponse"+i+" = " + classNameLowerCase + "." + methodName + "(" + StringUtils.join(params, ",") + ");");
                list.add(returnName + " logResponse"+i+" = JSON.parseObject("+ ValUtils.getLargeText(responseJson,outFilePath) +", new TypeReference<" + returnName + ">() {});");
                list.add("  ");
                list.add("//InvocationMatcher.mathRecordLog("+count+");");
                list.add("   ");
                list.add("//then");
                // 担心不准 先不强校验
                //list.add("Assert.assertEquals("+MOCKRECORDCOUNT+",InvocationMatcher.matchSuccessRecord.size());");
                if (Objects.equals(returnName, "com.akuchen.platform.components.mq.constants.MQMessageStatus")) {
                    list.add("Assert.assertEquals(alResponse"+i+",logResponse"+i+");");
                } else {
                    list.add("Assert.assertEquals(alResponse"+i+".getCode(),logResponse"+i+".getCode());");
                }
                if (Objects.equals("[trace-rpc-provider]", type)) {
                    list.add("if(alResponse"+i+".getCode().equals(\"0\")){");
                    list.add("    Assert.assertEquals(alResponse"+i+".getMessage(),logResponse"+i+".getMessage());");
                    list.add("    Assert.assertEquals(alResponse"+i+".getData(),logResponse"+i+".getData());");
                    list.add("}");
                }
                list.add("}catch(Exception e){");
                list.add("    e.printStackTrace();");
                list.add("}");
            }



        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        CodeInfoDTO codeInfoDTO = new CodeInfoDTO();
        codeInfoDTO.setHeadList(headList);
        codeInfoDTO.setBodyList(list);
        codeInfoDTO.setMethodList(methodList);
        codeInfoDTO.setType(1);
        return codeInfoDTO;
    }

}
