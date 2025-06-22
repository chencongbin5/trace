package com.akuchen.trace.report.http;

import com.akuchen.trace.api.common.dto.ApiResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.report.http.req.AddReportReqDTO;

import java.util.Objects;

@Slf4j
public class TraceReportHttp {

    private static String traceUrl= SystemConstant.HTTP_URL + "api/report/add";
    /**
     * 返回和es那边一样的消息体
     * @param tid
     * @param serviceName
     * @return
     */
    public static Boolean add(String tid, String serviceName, String logs){
        AddReportReqDTO reqDTO = new AddReportReqDTO();
        reqDTO.setTid(tid);
        reqDTO.setServiceName(serviceName);
        reqDTO.setLog(logs);
        String requestJson = JSON.toJSONString(reqDTO);
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                                                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestJson);
            Request request = new Request.Builder()
                    .url(traceUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            //log.info("TraceReportHttp response code:{}",response.code());
            if(Objects.equals(response.code(),200)){
                String result = response.body().string();
                //log.info("TraceReportHttp response :{}",result);
                ApiResult<String> parseObject = JSON.parseObject(result, new TypeReference<ApiResult<String>>() {
                });
                if(parseObject.isSuccess()){
                   return true;
                }
            }
        }catch (Exception e){
            log.error("TraceReportHttp error",e);
        }
        log.error("TraceReportHttp error tid:{},serviceName:{}",tid,serviceName);
        return false;
    }
}
