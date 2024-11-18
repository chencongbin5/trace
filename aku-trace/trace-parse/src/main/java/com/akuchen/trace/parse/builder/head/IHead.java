package com.akuchen.trace.parse.builder.head;

import com.akuchen.trace.parse.dto.CodeInfoDTO;

/**
 * 生成调用mock的方法的入口
 * dubbo 生成他的接口调用
 * http 生成他的http访问的调用
 *
 */
public interface IHead {

    CodeInfoDTO disposeHead(String log, String type, Integer count,String outFilePath);


}
