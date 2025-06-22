package com.akuchen.trace.parse.builder.head;

import com.akuchen.trace.parse.dto.CodeInfoDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HeadFactory {
    public static CodeInfoDTO doDisposeHead(List<String> lines, String type, Integer count,String outFilePath) {
        List<String> logs = lines.stream().filter(t -> t.indexOf(type) > -1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(logs)) {
            //取最长的字符串

            IHead head = HeadEnum.getHead(type);
            return head.disposeHead(logs.stream().max(Comparator.comparingInt(String::length)).get(), type,count,outFilePath);
        }
        return null;
    }
}
