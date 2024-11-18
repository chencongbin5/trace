package com.akuchen.trace.parse.jdi.rmi;

import lombok.extern.slf4j.Slf4j;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;

/**
 * 客户端
 */
@Slf4j
public class RmiClient {

    private static RmiClient instance;

    public static RmiClient getInstance() {
        if (instance == null) {
            instance = new RmiClient();
        }
        return instance;
    }
    private RmiClient(){

    }

    /**
     * 调用服务端的notifyCompleted方法
     */
    public Integer notifyCompleted() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", RmiServer.PORT);

            RemoteInterface  remote =(RemoteInterface) registry.lookup("remote");
            Integer result = remote.notifyCompleted();

            log.info("notifyCompleted result: " + result);
            return result;
        } catch (Exception e) {
           log.error(e.getMessage(),e);
        }
        return null;
    }
}
