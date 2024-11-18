package com.akuchen.trace.parse.builder.head;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.akuchen.trace.parse.builder.MockClassBuilder;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.utils.ApolloUtils;
import com.akuchen.trace.parse.utils.ValUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BizHeadImpl implements IHead{
    @Override
    public CodeInfoDTO disposeHead(String log, String type, Integer count,String outFilePath) {
        //获取入口
        List<String> list = new ArrayList<>();
        List<String> methodList=new ArrayList<>();
        List<String> headList = new ArrayList<>();

        List<String> importList=new ArrayList<>();
        List<String> anotationsList=new ArrayList<>();
        try {
            String classAndMethod = log.substring(log.indexOf(type) + (type.length() + 1), log.indexOf("]【request"));
            String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
            String methodName = classAndMethod.substring(classAndMethod.lastIndexOf(".") + 1);
            String requestJson = log.substring(log.indexOf("request=") + 8, log.indexOf("】"));
            String headersJson = log.substring(log.indexOf("headers=") + 8, log.indexOf("】【response"));
            String responseJson = log.substring(log.indexOf("response=") + 9, log.indexOf("】[cost"));
            Class<?> aClass = ClassUtils.getClass(className);
            String classNameLowerCase = MockClassBuilder.firstToLowerCase(className.substring(className.lastIndexOf(".") + 1));

            //import
            importList.add("import org.springframework.test.web.servlet.MockMvc;");
            importList.add("import org.springframework.test.web.servlet.ResultActions;");
            importList.add("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;");
            importList.add("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;");
            importList.add("import org.springframework.test.web.servlet.MvcResult;");
            importList.add("import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;\n");

            //anotation
            anotationsList.add("@AutoConfigureMockMvc\n");

            //head
            headList.add("@Autowired    private MockMvc mockMvc;");


            Method[] methods = aClass.getDeclaredMethods();
            Method method = Arrays.stream(methods).filter(t -> Objects.equals(t.getName(), methodName)).findFirst().orElse(null);
            //String paramName = Arrays.stream(method.getGenericParameterTypes()).findFirst().get().getTypeName();
            String returnName = method.getGenericReturnType().getTypeName();



            //方法名
            methodList.add(methodName+"_"+ ValUtils.valAdd());
            //结尾
            list.add("//"+classAndMethod);//给kucode 准备的定位信息
            //暂时禁用  如果要恢复,多线程那边的入参匹配就的改成非any
            list.add("//InvocationMatcher.matchRecordInit();");
            List<String> params = MockClassBuilder.assemblyParam(JSON.parseArray(requestJson), method.getGenericParameterTypes(), classAndMethod,false);
            //list.add(paramName + " param=JSON.parseObject(\"" + valAdd(requestJson) + "\", new TypeReference<" + paramName + ">() {});");
            list.add(" ");
            list.add("//跳过指定字段 Equality.addField(...);");
            //配置信息修改
            Optional.ofNullable(ApolloUtils.getWrhiteListFields()).ifPresent(fields->{
                fields.forEach(t->{
                    list.add("//Equality.addField(\""+t+"\");");
                });
            });
            list.add("    ");
            list.add("//when");
            list.add("ResultActions perform = mockMvc.perform(post(\""+url(aClass,method)+"\")");
            list.add(".contentType(\""+produce(method)+"\")");

            list.add(".content(\""+MockClassBuilder.valAdd(requestJson.substring(1,requestJson.length()-1))+"\")");

            JSONObject parse = (JSONObject) JSON.parse(headersJson);
            parse.forEach((k,v)->{
                list.add(".header(\""+StringEscapeUtils.escapeJava(k)+"\", \""+StringEscapeUtils.escapeJava(v.toString())+"\")");
                // list.add(".header(\""+k+"\", \""+v+"\")");
            });
            list.add(");");

//            list.add(returnName + " alResponse = " + classNameLowerCase + "." + methodName + "(" + StringUtils.join(params, ",") + ");");
//            list.add(returnName + " logResponse=JSON.parseObject(\"" + MockClassBuilder.valAdd(responseJson) + "\", new TypeReference<" + returnName + ">() {});");
//            list.add("  ");
//            list.add("InvocationMatcher.mathRecordLog("+count+");");

            list.add("MvcResult mvcResult = perform.andReturn();");
            list.add("String contentAsString = mvcResult.getResponse().getContentAsString();");
            list.add(returnName + " alResponse = JSON.parseObject(contentAsString, new TypeReference<" + returnName + ">() {});");
            list.add(returnName + " logResponse=JSON.parseObject("+ValUtils.getLargeText(responseJson,outFilePath) +", new TypeReference<" + returnName + ">() {});");
            list.add("  ");
            list.add("//InvocationMatcher.mathRecordLog("+count+");");
            list.add("   ");
            list.add("//then");
            // 担心不准 先不强校验
            //list.add("Assert.assertEquals("+MOCKRECORDCOUNT+",InvocationMatcher.matchSuccessRecord.size());");
            list.add("Assert.assertEquals(alResponse.isSuccess(),logResponse.isSuccess());");
            list.add("perform.andExpect(status().isOk());");





        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        CodeInfoDTO codeInfoDTO = new CodeInfoDTO();
        codeInfoDTO.setImportList(importList);
        codeInfoDTO.setAnotationsList(anotationsList);
        codeInfoDTO.setHeadList(headList);
        codeInfoDTO.setBodyList(list);
        codeInfoDTO.setMethodList(methodList);
        codeInfoDTO.setType(1);
        return codeInfoDTO;
    }

    private String url(Class<?> aClass, Method method){
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        String methodUrl = postMapping.value()[0];
        RequestMapping requestMapping = aClass.getAnnotation(RequestMapping.class);
        String classUrl = requestMapping.value()[0];
        String url = "/"+classUrl + methodUrl;
        url=url.replaceAll("//", "/");
        return url;
    }

    private String produce(Method method){
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        //数组有值 取第一个
        if (postMapping.produces().length > 0) {
            return postMapping.produces()[0];
        } else {
            return MediaType.APPLICATION_JSON_VALUE;
        }
    }



}
