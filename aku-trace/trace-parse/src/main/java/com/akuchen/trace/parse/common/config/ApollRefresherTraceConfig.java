//package com.akuchen.trace.parse.common.config;
//
//import com.ctrip.framework.apollo.model.ConfigChangeEvent;
//import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
//import lombok.extern.slf4j.Slf4j;
//import org.mockito.internal.matchers.Equality;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Arrays;
//import java.util.Set;
//
//@Configuration
//@Slf4j
//public class ApollRefresherTraceConfig {
//
//
//
//    /**
//     * 监听apollo变更 修改线程池大小
//     * @param event
//     */
//
//    @ApolloConfigChangeListener({"application.yml"})
//    public void changeListener(ConfigChangeEvent event) {
//
//        try {
//            //1 变更信息
//            Set<String> updatedKeys = event.changedKeys();
//            for (String key : updatedKeys) {
//                String[] split = key.split("\\.");
//                ///处理特定的格式=====order-background-service.monitorTypePermits.1
//                if (key.startsWith("trace.wrhiteListFields")) {
//                    //1 获取type
//                    String newValue = event.getChange(key).getNewValue();
//                    Equality.WRHITE_LIST_FIELDS=Arrays.asList(newValue.split(","));
//                }
//            }
//        } catch (Exception e) {
//            log.error("线程池定时任务切换异常 检查apollo 参数order-background-service.monitorTypePermits", e);
//        }
//
//    }
//}
