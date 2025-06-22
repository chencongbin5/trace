package com.akuchen.trace.parse.jdi.rmi;

import lombok.extern.slf4j.Slf4j;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * 服务端注册好，客户端就可以调用了
 */
@Slf4j
public class RmiServer {

    public static Integer PORT = 1099; // RMI注册表的端口号
    private static Registry registry;
    public static void run() {
        try {
            registry = LocateRegistry.createRegistry(PORT);

            RemoteImpl remote = new RemoteImpl();
            registry.rebind("remote", remote);

            log.info("RMI Server is running...");
        } catch (Exception e) {
           log.error(e.getMessage(),e);
        }
    }

    public static void stop() {
        try {
            UnicastRemoteObject.unexportObject(registry, true);
            log.info("RMI Server has been stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
