package com.akuchen.trace.parse.jdi.listener;

import com.akuchen.trace.parse.common.GlobalVariables;
import com.akuchen.trace.parse.jdi.rmi.RmiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationListener;

@TestComponent
@Slf4j
public class StartUpListener implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        GlobalVariables globalVariables = GlobalVariables.getInstance();
        if(globalVariables.getEnableRmiClient()){
            log.info("onApplicationEvent...notifyCompleted");
            RmiClient rmiClient = RmiClient.getInstance();
            Integer status = rmiClient.notifyCompleted();
            log.info("onApplicationEvent...return status:{}", status);
        }

    }
}
