package com.akuchen.trace.parse.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * test启动项目的时候  检查类是不是有多个bean (dubbo消费者 代理bean 和mock bean),如果有多个且有一个是mockbean 用mockbean 覆盖代理bean
 */
@TestConfiguration
@Slf4j
public class BeanMockProcessor implements BeanPostProcessor {
    @Autowired
    private ApplicationContext applicationContext;

    private static List<String> PREFIX;

    static {
        PREFIX = new ArrayList<>();
        PREFIX.add("com.aku");
        PREFIX.add("com.al.");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            Field[] fields;
            Object contextBean = null;
            if (AopUtils.isCglibProxy(bean)) {
                contextBean= AopProxyUtils.getSingletonTarget(bean);
                fields = Optional.ofNullable(contextBean).map(t -> t.getClass().getDeclaredFields()).orElse(null);
            } else {
                //普通类
                fields = bean.getClass().getDeclaredFields();
            }

            if (fields == null || fields.length == 0) {
                return bean;
            }

            for (Field field : fields) {

                //dubbo接口 field type 都是有特征的 如果全路径不在公司规范的包下面就不搭理他了,
                boolean flag = PREFIX.stream().anyMatch(t -> field.getType().getName().contains(t));
                if (!flag) {
                    continue;
                }
                Map<String, ?> beansOfType = applicationContext.getBeansOfType(field.getType());
                if (beansOfType.size() > 1) {

                    for (Map.Entry<String, ?> stringEntry : beansOfType.entrySet()) {
                        if (stringEntry.getValue().getClass().getName().contains("$MockitoMock$")) {
                            try {
                                field.setAccessible(true);
                                //代理类 就直接去取
                                if (AopUtils.isCglibProxy(bean)) {
                                    field.set(contextBean, stringEntry.getValue());
                                } else {
                                    //普通类
                                    field.set(bean, stringEntry.getValue());
                                }

                            } catch (IllegalAccessException e) {
                                log.error("mock覆盖失败 bean:{} mockBean:{}", beanName, stringEntry.getKey());
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return bean;
    }
}
