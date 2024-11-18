package com.akuchen.trace.parse.http;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.api.common.dto.ApiResult;
import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
import com.akuchen.trace.parse.utils.HttpQueryUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class TraceQueryHttp {

    private static String traceUrl= SystemConstant.HTTP_URL + "api/traceLog/query";
    /**
     * 返回和es那边一样的消息体
     * @param tid
     * @param serviceName
     * @return
     */
    public static List<String> queryLog(String tid, String serviceName){
        String requestJson = "{\"tid\":\""+tid+"\",\"serviceName\":\""+serviceName+"\"}";

        try {
            Response response = HttpQueryUtils.query(traceUrl, requestJson, null);
            if(Objects.equals(response.code(),200)){
                String result = response.body().string();
                log.info("queryLog response :{}",result);
                ApiResult<List<TraceLogMsgDTO>> parseObject = JSON.parseObject(result, new TypeReference<ApiResult<List<TraceLogMsgDTO>>>() {
                });
                if(parseObject.isSuccess()){
                    List<TraceLogMsgDTO> data = parseObject.getData();
                    return data.stream().map(t->t.getThreadName()+"|"+t.getLog()).collect(Collectors.toList());
                }
            }
        }catch (Exception e){
            log.error("queryLog error",e);
        }
        log.error("queryLog error tid:{},serviceName:{}",tid,serviceName);
        return null;
    }
}
