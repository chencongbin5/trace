package com.akuchen.trace.parse.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.mockito.internal.matchers.Equality;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * springboot test启动用到的
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "trace")
@TestConfiguration
public class BeanConfig {

    private List<String> wrhiteListFields;


    @PostConstruct
    public void init(){
        if(CollectionUtils.isNotEmpty(wrhiteListFields)){
            wrhiteListFields.stream().forEach(field->{
                Equality.addField(field);
            });
        }
    }

}
