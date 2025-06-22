package com.akuchen.trace.parse.jdi.rmi;

import com.akuchen.trace.parse.enums.StatusEnum;
import com.akuchen.trace.parse.jdi.JdiMethodManager;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 远程调用实现类
 */
@Slf4j
public class RemoteImpl extends UnicastRemoteObject implements RemoteInterface{

    protected RemoteImpl() throws RemoteException {
        super();
    }


    @Override
    public Integer notifyCompleted() throws RemoteException {
        Integer type = StatusEnum.COMPELETED.getType();
        //todo 注册debugger事件
        //JdiMethodManager jdiMethodManager = JdiMethodManager.getInstance();
        log.info("server start to debugger ...");
        //jdiMethodManager.registerEvent();
        log.info("server start to debugger success...");
        return type;
    }
}
