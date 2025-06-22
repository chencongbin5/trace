package com.akuchen.trace.parse.builder;

import com.akuchen.trace.parse.builder.head.HeadEnum;
import com.akuchen.trace.parse.builder.head.HeadFactory;
import com.akuchen.trace.parse.builder.head.IHead;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;
import com.akuchen.trace.parse.dto.TemplateReq;
import com.akuchen.trace.parse.enums.TypeEnum;
import com.akuchen.trace.parse.utils.ListUtils;
import com.akuchen.trace.parse.utils.ValUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 根据rpc日志生成方法的mock脚本
 */
@Slf4j
public class MockClassBuilder {


    private static String ANY = "any()";

    /**
     * 使用说明
     * 此方案是集成测试核心接口的入口,mock所有的外部接口,只关注自身业务逻辑变更后的断言是否和当初一致 ,如果外部接口加参数 或者换接口,对应的mock需要重新设置
     * 前置工作
     * 新建一个RpcLogFilter                修改 String serviceName = invoker.getInterface().getName();(原方法这里取的是接口类名 不是完整类名,不好搞啊)
     * 变更com.alibaba.dubbo.rpc.Filter   修改  accessLogFilter=com.akuchen.service.calculation.filter.RpcLogFilter
     * 开始
     * 1正常执行某接口方法
     * 2把日志复制到json/response/MethodMockBuilder.log
     * 3执行play方法
     * 4把输出的代码复制到测试given上
     */

    protected static QueryLogAndCreateClassFileReq req;

    /**
     * 外部接口用
     * @param lines
     */
    public static List<CodeInfoDTO> start(List<String> lines, QueryLogAndCreateClassFileReq req) {
        MockClassBuilder.req =req;
        boolean isAny = req.isWhenParamsIsAny();
        ValUtils.valReset();

        //可能存在多个线程 分开取
        List<List<String>> groupLines = groupLines(lines);
        List<CodeInfoDTO> codeInfoDTOS = groupLines.stream().map(threadLines -> {
            CodeInfoDTO codeInfoDTO = new CodeInfoDTO(ListUtils.parseStringThread(threadLines.get(0)));
            //given 信息
            codeInfoDTO.getBodyList().add("//given");
            //[RPC]
            List<CodeInfoDTO> rpcInfos = doLines(threadLines, "[trace-rpc]",isAny);
            rpcInfos.forEach(rpcInfo -> codeInfoDTO.addCodeInfo(rpcInfo));
            //[SQL]
            List<CodeInfoDTO> sqlInfos = doLines(threadLines, "[trace-general]",isAny);
            sqlInfos.forEach(rpcInfo -> codeInfoDTO.addCodeInfo(rpcInfo));
            //[httpService]
            List<CodeInfoDTO> serviceInfos = doLines(threadLines, "[trace-httpService]",isAny);
            serviceInfos.forEach(rpcInfo -> codeInfoDTO.addCodeInfo(rpcInfo));
            //[请求发起入口]
            codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.RPC.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
            codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.MQ.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
            codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.JOB.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
            codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.BIZ.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
            //堆栈异常信息
            Map<String, List<DebuggerDTO>> map = ProcessStackTrace.filterErrorLog(threadLines);
            codeInfoDTO.setDebuggerMap(map);

            //check
            codeInfoDTO.check();

            return codeInfoDTO;
        }).filter(codeInfo -> codeInfo.getBodyList().size()>1).collect(Collectors.toList());
        //最后检查
        finalCheck(codeInfoDTOS);
        return codeInfoDTOS;

    }

    protected static void finalCheck(List<CodeInfoDTO> codeInfoDTOS){
        List<String> notMethodList = codeInfoDTOS.stream()
                                           .filter(t -> CollectionUtils.isEmpty(t.getMethodList()))
                                           .flatMap(t -> t.getBodyParamNonNullList().stream())
                                                 //后面写入的 因为param不能为空, 碰到多态情况时候难免报错, 加try  catch 报错行不mock就是了
                .map(t->"try{"+t+"}catch(Exception e){ log.error(\"mock error\",e);}")
                                           .collect(Collectors.toList());
        codeInfoDTOS.stream().filter(t -> CollectionUtils.isNotEmpty(t.getMethodList()))
                    .forEach(t -> {
                        List<String> bodyList = t.getBodyList();
                        bodyList.addAll(2, notMethodList);
                    });
    }

    protected static List<List<String>> groupLines(List<String> lines){
        Map<String, List<String>> linesGroupByThread = lines.stream().collect(Collectors.groupingBy(t -> ListUtils.parseStringThread(t)));

        //排序
        List<String> keys=new ArrayList<>();
        lines.stream().forEach(t ->{
            String key = ListUtils.parseStringThread(t);
            if (!keys.contains(key)){
                keys.add(key);
            }
        });

        List<List<String>> collect = keys.stream().map(key -> linesGroupByThread.get(key)).collect(Collectors.toList());
        //List<List<String>> collect = linesGroupByThread.entrySet().stream().map(t -> t.getValue()).collect(Collectors.toList());

        return collect;
    }



    protected static List<CodeInfoDTO> doLines(List<String> lines, String type, Boolean isAny) {
        List<String> rpcLines = lines.stream().filter(line -> line.indexOf(type) > -1).collect(Collectors.toList());
        return rpcLines.stream().map(line -> dispose(line, type,isAny)).collect(Collectors.toList());
    }

    private static CodeInfoDTO dispose(String log, String type, Boolean isAny) {

        List<String> list = new ArrayList<>();
        List<String> paramNonNullList = new ArrayList<>();
        List<String> mockList = new ArrayList<>();
        String classAndMethod = log.substring(log.indexOf(type) + (type.length() + 1), log.indexOf("]【request"));
        String className = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
        String methodName = classAndMethod.substring(classAndMethod.lastIndexOf(".") + 1);
        String requestJson = log.substring(log.indexOf("request=") + 8, log.indexOf("】【response="));
        String responseJson = log.substring(log.indexOf("response=") + 9, log.lastIndexOf("】"));
        //日志上的返回类型 不一定每个都有,
        String logReturnType = log.indexOf("[returnType=") > -1 ? log.substring(log.indexOf("[returnType=") + 12, log.lastIndexOf("]")) : null;
        Class<?> aClass = null;
        try {
            aClass = ClassUtils.getClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method[] methods = aClass.getMethods();
        List<Method> collect = Arrays.stream(methods).filter(t -> Objects.equals(t.getName(), methodName)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect) && collect.size() > 1) {
            list.add("//同类同方法名存在多条记录,请人工判断,清理并只保存一条 ##############start");
        }
        Class<?> finalAClass = aClass;
        collect.forEach(method -> {
            JSONArray requestJsonArray = JSON.parseArray(requestJson);
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            //如果参数长度对应不上,直接return 说明不是这一条
            if (requestJsonArray.size() != genericParameterTypes.length) {
                return;
            }

            //String paramName = Arrays.stream(method.getGenericParameterTypes()).findFirst().get().getTypeName();
            String returnTypeName = TypeEnum.convertType(method.getGenericReturnType().getTypeName());
            if (Objects.equals("keyService", firstToLowerCase(finalAClass.getSimpleName()))) {
                return;
            }
            String classNameLowerCase = firstToLowerCase(className.substring(className.lastIndexOf(".") + 1));
            String mockName = "@MockBean    private " + className + " " + classNameLowerCase + ";";
            mockList.add(mockName);
            if (Objects.equals(returnTypeName, "void")) {
                //当返回值是void的时候 没有必要模拟 直接donothing
                list            .add("doNothing().when(" + classNameLowerCase + ")." + methodName + "(" + StringUtils.join(assemblyParam(requestJsonArray, genericParameterTypes, classAndMethod,isAny), ",") + ");");
                paramNonNullList.add("doNothing().when(" + classNameLowerCase + ")." + methodName + "(" + StringUtils.join(assemblyParam(requestJsonArray, genericParameterTypes, classAndMethod,true), ",") + ");");
                return;
            }
            list            .add("when(" + classNameLowerCase + "." + methodName + "(" + StringUtils.join(assemblyParam(requestJsonArray, genericParameterTypes, classAndMethod,isAny), ",") + "))" + assemblyResponse((responseJson), returnTypeName, logReturnType));
            paramNonNullList.add("when(" + classNameLowerCase + "." + methodName + "(" + StringUtils.join(assemblyParam(requestJsonArray, genericParameterTypes, classAndMethod,true), ",") + "))" + assemblyResponse((responseJson), returnTypeName, logReturnType));
            //".thenReturn(JSON.parseObject(\"" + valAdd(responseJson) + "\",new TypeReference<" + returnTypeName + ">() {}));"
            //list.add(firstToLowerCase(zlazz.getSimpleName()) + ".set" + firstToUpCase(serviceName) + "(" + serviceName + ");");
        });
        if (CollectionUtils.isNotEmpty(collect) && collect.size() > 1) {
            list.add("//同类同方法名存在多条记录,请人工判断,清理并只保存一条 ##############end");
        }

        CodeInfoDTO codeInfoDTO = new CodeInfoDTO();
        codeInfoDTO.setHeadList(mockList);
        codeInfoDTO.setBodyList(list);
        codeInfoDTO.setBodyParamNonNullList(paramNonNullList);
        return codeInfoDTO;
    }

    /**
     * .then 文本
     *
     * @param
     * @param returnTypeName
     * @return
     */
    private static String assemblyResponse(String javaResponse, String returnTypeName, String logReturnType) {
        //javaResponse 普通字符串
        //responseJson 带了\"的字符串
        String responseJson = valAdd(javaResponse);
        String returnTypeNameResult = returnTypeName;
        try {
            if (responseJson.lastIndexOf("Exception") > -1) {
                Class<?> aClass = ClassUtils.getClass(responseJson);
                //1 获取这个类的构造方法
                Constructor<?>[] constructors = aClass.getDeclaredConstructors();
                //2 取最简单的这个 获取构造方法的参数
                Class<?>[] parameterTypes = constructors[0].getParameterTypes();
                //3 参数 尽可能给null
                String param = Arrays.stream(parameterTypes).map(t -> TypeEnum.convertDefaultValue(t.getName())).collect(Collectors.joining(","));
                //4 生成文本
                // 如果抛出来的异常 不属于 new RuntimeException("")   mockito验证不通过  那就都设置成RuntimeException吧 懒得搞了,
                //return ".thenThrow(new " + responseJson + "(" + Optional.ofNullable(param).orElse("") + "));";
                return ".thenThrow(new RuntimeException(\"mock error\"));";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        //发现是泛型 对象
        if (Objects.equals(returnTypeName, "T")) {
            if(Objects.equals(responseJson,"[]")){
                return ".thenReturn(null);";
            }
            //sql 从arraylist 的第一个对象取出泛型值
            returnTypeNameResult = logReturnType.substring(logReturnType.indexOf("<") + 1, logReturnType.indexOf(">"));
        }
        //list
        else if (returnTypeName.contains("java.util.ArrayList<T>")|| returnTypeName.contains("java.util.List<T>")) {
            if(Objects.equals(responseJson,"[]")){
                return ".thenReturn(Collections.EMPTY_LIST);";
            }
            returnTypeNameResult = logReturnType;
        }
        //如果string类型太长了 编译不通过  转成 FileUtils.readFileToString(new File("xxxxx"));
        if(javaResponse.length()>20000){
            String largeText = ValUtils.getLargeText(javaResponse, req.getOutFilePath());
            //指向文件
            return ".thenReturn(JSON.parseObject("+largeText+",new TypeReference<" + returnTypeNameResult + ">() {}));";
        }
        //普通对象或者泛型对象
        if (!returnTypeName.contains("java.util.ArrayList") && !returnTypeName.contains("java.util.List") && responseJson.startsWith("[")) {
            responseJson= responseJson.substring(0+1,responseJson.length()-1);
        }
        //string类型就直接返回
        if(Objects.equals(returnTypeNameResult,"java.lang.String")){
            return ".thenReturn(\"" + (responseJson) + "\");";
        }
        return ".thenReturn(JSON.parseObject(\"" + (responseJson) + "\",new TypeReference<" + returnTypeNameResult + ">() {}));";
    }

    /**
     * donothing 的时候 参数必须是any
     * @param
     * @param genericParameterTypes
     * @param classAndMethod
     * @param isAny
     * @return
     */
    public static List<String> assemblyParam(JSONArray array, Type[] genericParameterTypes, String classAndMethod,Boolean isAny) {
        List<String> list = new ArrayList<>();
        // 改成全是数组
        for (int i = 0; i < genericParameterTypes.length; i++) {
            String any = toAny(classAndMethod);
            if (StringUtils.isNotEmpty(any)) {
                list.add(ANY);
                continue;
            }
            TypeEnum typeEnum = TypeEnum.convertEnumType(genericParameterTypes[i].getTypeName());
            String ParamTypeName = TypeEnum.convertType(genericParameterTypes[i].getTypeName());
            //泛型参数处理不来,
            if (ParamTypeName.indexOf("<T>") > -1 ||
                    Objects.equals(ParamTypeName, "T")) {
                list.add(ANY);
                continue;
            }
            //处理不了的方法
            if(BooleanUtils.isTrue(isAny)){
                if (Objects.nonNull(typeEnum)){
                    list.add(typeEnum.getAnyValue());
                }else{
                    list.add(String.format("(%s)"+ANY,ParamTypeName));
                }
                continue;
            }
            //string类型就直接返回
            if(Objects.equals(ParamTypeName,"java.lang.String")){
                list.add("\"" + valAdd(array.get(i).toString()) + "\"");
                continue;
            }
            //可以处理的方法
            Object o = array.get(i);
            String s = Objects.isNull(o) ? null : valAdd(o.toString());
            list.add("JSON.parseObject(\"" + s + "\", new TypeReference<" + ParamTypeName + ">() {})");
        }
        return list;
    }


    private static File readClassPath(String filePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(filePath);
        return classPathResource.getFile();
    }

    public static String firstToLowerCase(String val) {
        char[] chars = val.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private static String firstToUpCase(String val) {
        char[] chars = val.toCharArray();
        chars[0] -= 32;
        return String.valueOf(chars);
    }

    public static String valAdd(String json) {
        return StringEscapeUtils.escapeJava(TypeEnum.convertType(json));
    }

    public static String toAny(String val) {
        List<String> list = Arrays.asList(
                "com.akuchen.service.pay.api.rpc.IInstallmentServiceApi.getBillDate",
                "com.akuchen.service.pay.api.rpc.IInstallmentServiceApi.getInstallmentPricingPlans" //参数有object  json转回来的mock对象 没法匹配实际的入参对象
        );
        return list.contains(val) ? ANY : null;
    }


    public static String template(TemplateReq templateReq,String suffix) {
        String code = "package " + templateReq.getPackageName() + ";\n" +
                "\n" +
                "import org.apache.commons.io.FileUtils;\n"+
                "import java.io.File;\n"+
                "import com.akuchen.trace.parse.common.GlobalVariables;\n"+
                "import org.junit.runner.JUnitCore;\n" +
                "import org.junit.runner.Request;\n" +
                "import org.junit.runner.Result;\n" +
                "import org.junit.runner.notification.Failure;\n"+
                "import com.alibaba.fastjson.TypeReference;\n" +
                "import java.math.BigDecimal;\n" +
                "import java.math.RoundingMode;\n" +
                "import lombok.extern.slf4j.Slf4j;\n" +
                "import org.mockito.internal.matchers.Equality;\n"+
                "import org.junit.Test;\n" +
                "import java.util.Collections;\n"+
                "import java.util.ArrayList;\n" +
                "import org.mockito.internal.invocation.InvocationMatcher;\n" +
                "import org.junit.Assert;\n" +
                "import java.io.IOException;\n" +
                "import com.alibaba.fastjson.JSON;\n" +
                "import org.junit.runner.RunWith;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.boot.test.context.SpringBootTest;\n" +
                "import org.springframework.boot.test.mock.mockito.MockBean;\n" +
                "import org.springframework.test.context.ActiveProfiles;\n" +
                "import org.springframework.test.context.junit4.SpringRunner;\n" +
                "import org.springframework.test.web.servlet.MockMvc;\n"+
                "import static org.mockito.Mockito.*;\n" +
                StringUtils.join(writeImport(templateReq.getCodeInfoDTOS()), "\n")+
                "\n" +
                "@ActiveProfiles(profiles = \"" + templateReq.getEnv() + "\")\n" +
                "@RunWith(SpringRunner.class)\n" +
                "@SpringBootTest( classes = {" + templateReq.getMainClass() + ".class})\n" +
                "@Slf4j\n" +
                StringUtils.join(writeAnotation(templateReq.getCodeInfoDTOS()), "\n")+
                "public class " + templateReq.getClassName()+suffix + "  {\n" +
                "\n" +
                //引用对象
                "    " + StringUtils.join(writeHead(templateReq.getCodeInfoDTOS()), "\n    ") +
                "\n" +
                "\n" +
                //main方法
                writeMain(templateReq)+
                "\n" +
                "\n" +
                //mock方法
                StringUtils.join(writeBody(templateReq.getCodeInfoDTOS()), "\n")+
                "}\n";
        return code;
    }

    protected static List<String> writeImport(List<CodeInfoDTO> codeInfoDTOS){
        return  codeInfoDTOS.stream()
                .filter(t -> t != null && t.getImportList() != null).flatMap(t -> t.getImportList().stream()).collect(Collectors.toSet()).stream().collect(Collectors.toList());
    }
    protected static List<String> writeAnotation(List<CodeInfoDTO> codeInfoDTOS){
        return  codeInfoDTOS.stream()
                .filter(t -> t != null && t.getAnotationsList() != null).flatMap(t -> t.getAnotationsList().stream()).collect(Collectors.toSet()).stream().collect(Collectors.toList());
    }

    protected static List<String> writeBody(List<CodeInfoDTO> codeInfoDTOS){
        //加开头和结尾的缩进
        return codeInfoDTOS.stream().map(t->"    " +
                StringUtils.join(t.getBodyList(), "\n        ")+
                "\n    ").collect(Collectors.toList());
    }

    protected static List<String> writeHead(List<CodeInfoDTO> codeInfoDTOS){
        //去重
        return  codeInfoDTOS.stream().flatMap(t -> t.getHeadList().stream()).collect(Collectors.toSet()).stream().collect(Collectors.toList());
    }

    protected static String writeMain(TemplateReq templateReq)  {
        //取第一个可用的方法, 这里可能同时存在多个方法 咋办呢 先取第一个吧,
//        List<String> methods = templateReq.getCodeInfoDTOS().stream().flatMap(t -> t.getMethodList().stream()).collect(Collectors.toList());
//        List<String> threads = templateReq.getCodeInfoDTOS().stream().map(t -> t.getThreadName()).collect(Collectors.toList());
        //优先找有异常信息的线程 没有就取第一个
        CodeInfoDTO codeInfoDTO = templateReq.getCodeInfoDTOS().stream()
                .filter(t->CollectionUtils.isNotEmpty(t.getMethodList()))
                .findFirst().orElse(templateReq.getCodeInfoDTOS().stream().filter(t->MapUtils.isNotEmpty(t.getDebuggerMap())).findFirst().orElse(templateReq.getCodeInfoDTOS().get(0)));
        return "    public static void main(String[] args) {\n" +
                "        if(args.length>0 && args[0].equals(\"rmi\")){\n" +
                "            GlobalVariables globalVariables = GlobalVariables.getInstance();\n" +
                "            globalVariables.setEnableRmiClient(true);\n" +
                "        }\n"+
                "        Request request = Request.method("+templateReq.getClassName()+".class, \""+codeInfoDTO.getMethodList().get(0)+"\");\n" +
                "        Result result = new JUnitCore().run(request);\n" +
                "        for (Failure failure : result.getFailures()) {\n" +
                "            System.out.println(failure.toString());\n" +
                "        }\n" +
                "        System.out.println(\"Tests successful: \" + result.wasSuccessful());\n" +
                "        System.exit(0);\n"+
                "    }";
    }


}
