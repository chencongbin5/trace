package com.akuchen.trace.parse.jdi;

import com.akuchen.trace.api.common.dto.TraceOrderDTO;
import com.akuchen.trace.parse.dto.CompletionsRequest;
import com.akuchen.trace.parse.dto.CompletionsResponse;
import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.akuchen.trace.parse.http.ChatGPThttp;
import com.akuchen.trace.parse.jdi.connect.JvmConnect;
import com.akuchen.trace.parse.utils.ClassUtils;
import com.akuchen.trace.parse.utils.ValueConverter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.tools.jdi.StringReferenceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Data
public class JdiMethodManager {
    private VirtualMachine vm;
    private Process process;
    private EventRequestManager eventRequestManager;
    private EventQueue eventQueue;
    private EventSet eventSet;
    private boolean vmExit = false;
    private ThreadReference threadReference;
    //write your own testclass
    //private String testClassName = "com.akuchen.trace.parse.ccb";
    private String testClassName;
    private Class startMainClass;
    //断点的类
    private List<DebuggerDTO> debuggerDTOS;
    private String gptFilePath;


    private static JdiMethodManager instance;


    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static JdiMethodManager getInstance() {
        if (instance == null) {
            instance = new JdiMethodManager();
        }
        return instance;
    }

    private JdiMethodManager() {

    }


    public void run() throws Exception {

        //启动被调试的jvm项目
        launchDebugee();
        //注册方法创建和关闭事件
        registerEvent();
        //process赋值
        //processDebuggeeVM();
        //循坏获取触发的时间,并处理  debugger触发事件在这里注册,不然可能存在这个类还没创建 注册就来了, debugger触发事件需要找到类,找到方法确定第几行 所以写在方法注册成功后
        eventLoop();
        //关闭被调试的jvm项目
        // destroyDebuggeeVM();

    }

    public VirtualMachine launchDebugee() {
        //方式1  创建jvm 并连接
        //vm = JvmConnect.launchConnect(testClassName);
        //方式2  附着jvm 并连接
        //vm = JvmConnect.attachConnect("5005");
        //方式3  监听jvm 并连接
        vm = JvmConnect.listenConnect("5005");
        return vm;
    }


    public void processDebuggeeVM() {
        process = vm.process();
        log.info("process id:{}", JSON.toJSONString(process));
    }

    public void destroyDebuggeeVM() {
        process.destroy();
    }


    private static String getSourceLineText(Location location) {
        try {
            // 获取源文件路径
            String sourcePath = location.sourcePath();
            // 获取行号
            int lineNumber = location.lineNumber();
            // 读取源文件的指定行


            File file = new File(sourcePath);
            try (FileReader fileReader = new FileReader(file); LineNumberReader lineNumberReader = new LineNumberReader(fileReader)) {
                String line;
                while ((line = lineNumberReader.readLine()) != null) {
                    if (lineNumberReader.getLineNumber() == lineNumber) {
                        return line;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void registerEvent() {
        eventRequestManager = vm.eventRequestManager();


        //类加载事件注册
        ClassPrepareRequest cpr = eventRequestManager.createClassPrepareRequest();
        for (DebuggerDTO debuggerDTO : debuggerDTOS) {
            cpr.addClassFilter(debuggerDTO.getClassName());  // 只接收指定类的加载事件
            cpr.enable();
        }



    }

    private void eventLoop() throws Exception {
        System.out.println("class size:" + vm.allClasses().size());
        System.out.println(vm.classesByName(testClassName));

        eventQueue = vm.eventQueue();
        while (true) {
            if (vmExit == true) {
                log.info("eventLoop end:{}", vmExit);
                break;
            }
            eventSet = eventQueue.remove();
            EventIterator eventIterator = eventSet.eventIterator();
            while (eventIterator.hasNext()) {
                Event event = (Event) eventIterator.next();
                execute(event);
                if (!vmExit) {
                    eventSet.resume();
                }
            }
        }
    }

    private void execute(Event event) throws Exception {
        if (event instanceof VMStartEvent) {
            log.info("VM VMStartEvent");
        } else if (event instanceof ClassPrepareEvent) {
            //类加载事件
            ClassPrepareEvent cpe = (ClassPrepareEvent) event;
            ReferenceType referenceType = cpe.referenceType();
            DebuggerDTO debuggerDTO = debuggerDTOS.stream().filter(t -> Objects.equals(t.getClassName(), referenceType.name())).findFirst().orElse(null);

            List<Location> locations = referenceType.allLineLocations();

            // 遍历所有位置，找到包含目标文本的位置
            for (Location location : locations) {
                if(location.lineNumber() == debuggerDTO.getClassLine()){
                    BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(location);
                    //设置断点请求的相关参数，例如是否暂停在每个断点位置，是否在断点命中时自动恢复，以及其他特定的条件
                    breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);  // 暂停所有线程
                    breakpointRequest.enable();  // 启用断点请求
                    log.info("断点注册成功 类{},行{}", debuggerDTO.getClassName(), debuggerDTO.getClassLine());
                }
            }

        } else if (event instanceof MethodEntryEvent) {
            Method method = ((MethodEntryEvent) event).method();
            log.info("MethodEntryEvent -> Method: " + method.name() + ", Signature:" + method.signature());
            //当main方法加载的时候 再来设置断点
//            if(Objects.equals(method.name(),"main")){
//                //断点
//                Location location=vm.classesByName(testClassName).get(0).methodsByName(debuggerMethod).get(0).allLineLocations().get(debuggerLine);
//                BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(location);
//                //设置断点请求的相关参数，例如是否暂停在每个断点位置，是否在断点命中时自动恢复，以及其他特定的条件
//                //breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);  // 暂停线程
//                breakpointRequest.enable();  // 启用断点请求
//            }
        } else if (event instanceof MethodExitEvent) {
            log.info("MethodExitEvent");
        } else if (event instanceof VMDisconnectEvent) {
            vmExit = true;
            log.info("VMDisconnectEvent");
        }
        else if (event instanceof BreakpointEvent) {
            //如果配置了断点事件  但是没有生效 应该是运行到这一行之前就报错了, 比如没有引用其他的类
            BreakpointEvent breakpointEvent = (BreakpointEvent) event;
            log.info("Breakpoint hit: " + breakpointEvent.location());
            threadReference = breakpointEvent.thread();
            StackFrame stackFrame = threadReference.frame(0);

            // 获取局部变量列表
            List<LocalVariable> localVariables = stackFrame.visibleVariables();

            Map<String, Value> valueMap = new HashMap<>();
            for (int i = 0; i < localVariables.size(); i++) {
                log.info("thread" + threadReference.status());
                LocalVariable localVar = localVariables.get(i);
                String name = localVar.name();
                Value value = stackFrame.getValue(localVar);
                valueMap.put(name, value);
            }
            //todo  只打印需要的变量
            String className = breakpointEvent.location().declaringType().name();
            int lineNumber = breakpointEvent.location().lineNumber();
            //代码行
            String codeLine = ClassUtils.getCodeLine(startMainClass, className, Arrays.asList(lineNumber));
            //代码上下文
            String codeContent = ClassUtils.getCodeLine(startMainClass, className, ClassUtils.calMoreLines(lineNumber, 5, 2));
            log.info("codeline :" + codeLine);
            log.info("codecontent :" + codeContent);
            //找变量
            Map<String, Value> variables;
            if (codeLine.contains("throw")) {
                //找上下文变量
                variables = findVariable(codeContent, valueMap);
            } else {
                //找异常行变量
                variables = findVariable(codeLine, valueMap);
            }

            //可用的变量打印
            StringBuilder chatGptVariables=new StringBuilder();
            variables.forEach((k, v) -> {
                log.info("Variable: " + k);
                if (v instanceof PrimitiveValue) {
                    log.info("value: " + ((PrimitiveValue) v));
                }
                Object result = findActualValueV2(v);
                log.info("value: " + result);
                chatGptVariables.append("\n变量:'").append(k).append("',变量值:'").append(JSON.toJSONString(result)).append("'. ");
            });
            //堆栈
            String stack = debuggerDTOS.stream().filter(t -> Objects.equals(t.getClassName(), className)).map(DebuggerDTO::getStack).findFirst().orElse(debuggerDTOS.get(0).getStack());

            //找其他可用常量


            //枚举

//            异常 读不到枚举类
//            ClassType  enumClass  =(ClassType) vm.classesByName("com.akuchen.service.orderservice.enums.OrderItemDeliveryStatus").get(0);
//            Field enumField  = enumClass.fieldByName("WAIT");
//            Value enumClassValue = enumClass.getValue(enumField);
//            Object valueV2 = findActualValueV2(enumClassValue);
//            log.info("eunmis: " + valueV2);

            //发送gpt
            CompletionsRequest request = new CompletionsRequest();
            String question = "堆栈:'"+stack+"'\n错误行:'" + codeLine + "'.       \n" + "错误上下文:\n'" + codeContent + "'.          " + "\n" + chatGptVariables;
            request.addUserMessage(question);
            //另外还需要常量的信息 我草,

            //堆栈不要,已经有锁定错误行了
            //+"这是异常堆栈信息:"+stack
            CompletionsResponse completion = ChatGPThttp.completion(request);
            if (Objects.nonNull(completion) && Objects.nonNull(completion.getChoices())){
                String answer = "现场:\n"+question +"\n 解释:\n"+ completion.getChoices().get(0).getMessage().getContent() ;
//                FileUtils.write(new File(gptFilePath),"问:\n"+question,"UTF-8",false);
//                FileUtils.write(new File(gptFilePath), "\n chatGPT答:\n"+ completion.getChoices().get(0).getMessage().getContent(),"UTF-8",true);
                TraceOrderDTO traceOrderDto=new TraceOrderDTO();
                traceOrderDto.setErrorClassName(className);
                traceOrderDto.setErrorMethodName("");
                traceOrderDto.setErrorLine(lineNumber);
                traceOrderDto.setErrorCodeBlock(codeLine);
                traceOrderDto.setErrorCodeBlockContext(codeContent);
                traceOrderDto.setGptAnswer(answer);
                FileUtils.write(new File(gptFilePath),JSON.toJSONString(traceOrderDto));
                log.info("gpt.log写入成功,路径{},文本{}",gptFilePath,JSON.toJSONString(traceOrderDto));
            }


            // 遍历局部变量列表并打印信息
//            for (int i = 0; i < localVariables.size(); i++) {
//                System.out.println("thread" + threadReference.status());
//                LocalVariable localVar = localVariables.get(i);
//                Value value = stackFrame.getValue(localVar);
//                System.out.println("Variable: " + localVar.name());
//                System.out.println("Type: " + localVar.type().name());
//                System.out.println("obj: " + (value));
//                if (value instanceof PrimitiveValue) {
//                    System.out.println("value: " + ((PrimitiveValue) value));
//                    continue;
//                }
//                Object result = findActualValueV2(value);
//                System.out.println("value: " + result);
//            }
        }
    }

    private Map<String, Value> findVariable(String code, Map<String, Value> valueMap) {
        Set<String> set = new HashSet<>();
        String[] tokens = code.split("\\s+|\\(|\\)|\\.|\\[|\\]|\\{|\\}|,|;");
        //tokens 是否包含code

        valueMap.entrySet().removeIf(entry -> Arrays.stream(tokens).noneMatch(token -> Objects.equals(token, entry.getKey())));
        return valueMap;

//        return valueMap.entrySet().stream().filter(entry -> Arrays.stream(tokens).anyMatch(token -> Objects.equals(token, entry.getKey())))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        for (String token : tokens) {
//            log.info(token);
//            boolean present = Optional.ofNullable(valueMap.get(token)).isPresent();
//            if (present) {
//                set.add(token);
//            }
//        }
//        List<String> collect = set.stream().collect(Collectors.toList());
    }

    private Object findActualValueV2(Value value) {
        if (value instanceof ObjectReference) {
            ObjectReference objectRef = (ObjectReference) value;
            ReferenceType referenceType = objectRef.referenceType();
            if (value instanceof PrimitiveValue) {
                return (PrimitiveValue) value;
            }
            Object o = ValueConverter.convertValue(value, threadReference);
            if (Objects.nonNull(o)) {
                return o;
            }
//            Object obj = ObjUtils.createObj(referenceType.name());
//            List<Field> fields = referenceType.allFields();
//            for (Field field : fields) {
//                java.lang.reflect.Field tmp = ObjUtils.getField(obj, field.name());
//                if (Objects.nonNull(tmp)) {
//                    Value fieldValue = objectRef.getValue(field);
//                    tmp.set(obj, findActualValueV2(fieldValue));
//                }
//            }
//            return obj;
        }
        return null;
    }

    /**
     * 递归把value转换成 普通值或者json对象
     *
     * @param value
     * @return
     */
    private Object findActualValue(Value value) {
        if (value instanceof ObjectReference) {
            ObjectReference objectRef = (ObjectReference) value;
            ReferenceType referenceType = objectRef.referenceType();
            if (referenceType.name().equals("java.lang.Integer")) {
                return objectRef.getValue(referenceType.fieldByName("value")).toString();
            }
            if (referenceType.name().equals("java.lang.String")) {
                return ((StringReferenceImpl) objectRef).value();
            }
            if (referenceType.name().equals("java.lang.Boolean")) {
                return objectRef.getValue(referenceType.fieldByName("value")).toString();
            }
            JSONObject jsonObject = new JSONObject();
            List<Field> fields = referenceType.allFields();
            for (Field field : fields) {
                Value fieldValue = objectRef.getValue(field);
                jsonObject.put(field.name(), findActualValue(fieldValue));
            }
            return jsonObject;
        }
        return null;
    }
}
