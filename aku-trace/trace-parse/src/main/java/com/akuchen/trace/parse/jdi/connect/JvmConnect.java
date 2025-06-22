package com.akuchen.trace.parse.jdi.connect;

import com.akuchen.trace.parse.utils.MavenUtils;
import com.alibaba.fastjson.JSON;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.*;
import com.sun.tools.attach.VirtualMachineDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JvmConnect {
    /**
     * 当前jvm 为监听类, 额外启动jvm2 为业务类
     *
     * @param testClassName
     * @return
     */
    public static VirtualMachine launchConnect(String testClassName) {
        LaunchingConnector launchingConnector = Bootstrap
                .virtualMachineManager().defaultConnector();

        // Get arguments of the launching connector
        Map<String, Connector.Argument> defaultArguments = launchingConnector
                .defaultArguments();
        Connector.Argument mainArg = defaultArguments.get("main");
        Connector.Argument suspendArg = defaultArguments.get("suspend");

        //有这个 被启动的jvm项目才会拿到各种引用信息
        defaultArguments.get("options").setValue("-cp " + MavenUtils.getMavenDependencies());
        mainArg.setValue(testClassName);
        suspendArg.setValue("true");
        try {
            //默认执行的是指定类的main方法 //main方法执行的时候再去触发想要执行的test方法
            return launchingConnector.launch(defaultArguments);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static VirtualMachine attachConnect(String port) {
        // 获取AttachingConnector
        AttachingConnector attachingConnector = null;
        List<AttachingConnector> attachingConnectors = Bootstrap.virtualMachineManager().attachingConnectors();
        for (AttachingConnector connector : attachingConnectors) {
            if (connector.name().equals("com.sun.jdi.SocketAttach")) {
                attachingConnector = connector;
                break;
            }
        }

        // 获取附加连接器的参数
        Map<String, Connector.Argument> defaultArguments = attachingConnector.defaultArguments();
        Connector.Argument hostnameArg = defaultArguments.get("hostname");
        Connector.Argument portArg = defaultArguments.get("port");

        // 设置连接参数
        hostnameArg.setValue("127.0.0.1");
        //portArg.setValue("8000");
        portArg.setValue(port);

        // 连接到虚拟机
        try {
            return attachingConnector.attach(defaultArguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static VirtualMachine listenConnect(String port) {
        // 获取ListeningConnector
        ListeningConnector listeningConnector = null;
        for (ListeningConnector connector : Bootstrap.virtualMachineManager().listeningConnectors()) {
            if (connector.name().equals("com.sun.jdi.SocketListen")) {
                listeningConnector = connector;
                break;
            }
        }

        // 获取监听连接器的参数
        Map<String, Connector.Argument> defaultArguments = listeningConnector.defaultArguments();
        Connector.Argument portArg = defaultArguments.get("port");
        Connector.Argument addressArg = defaultArguments.get("localAddress");

        // 设置监听参数
        addressArg.setValue("localhost");
        portArg.setValue(port);
        // 启动监听器
        try {
            VirtualMachine virtualMachine = listeningConnector.accept(defaultArguments);
            log.info("listenConnect: {}", JSON.toJSONString(virtualMachine));
            log.info("process: {}", JSON.toJSONString(virtualMachine.process()));
            return virtualMachine;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("listenConnect error");
        return null;
    }



}
