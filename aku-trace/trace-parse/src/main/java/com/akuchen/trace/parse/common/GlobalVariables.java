package com.akuchen.trace.parse.common;

import lombok.Data;

@Data
public class GlobalVariables {
    private static GlobalVariables instance;
    private GlobalVariables() {
        // 私有构造函数
    }
    public static GlobalVariables getInstance() {
        if (instance == null) {
            instance = new GlobalVariables();
        }
        return instance;
    }


    //是否启用rmi客户端
    private Boolean enableRmiClient = false;


}

