package com.akuchen.trace.service.util;

import com.akuchen.trace.service.common.TraceConfig;
import com.akuchen.trace.service.entity.dto.rsp.BaseRsp;
import com.akuchen.trace.service.entity.dto.rsp.TapdBugRsp;
import com.akuchen.trace.service.entity.dto.rsp.TapdCommentRsp;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

@Slf4j
@Component
public class TapdHttpClient {

    @Autowired
    private  TraceConfig traceConfig;


    /**
     * 查询所有bug
     * @param startDate
     * @param endDate
     * @return
     */
    public List<TapdBugRsp> bugsWithNonNullTid(String startDate,String endDate) {
        OkHttpClient client = getUnsafeOkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"createStartDate\":\""+startDate+"\",\r\n\"createEndDate\":\""+endDate+"\"}");
        Request request = new Request.Builder()
                .url(traceConfig.getTapdPreFix()+"/iapi/tapd/workspace/"+traceConfig.getTapdWorkspaceId()+"/bug/bugsWithNonNullTid")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", traceConfig.getAuthorization())
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                BaseRsp<List<TapdBugRsp>> baseRsp = JSON.parseObject(responseBody, new TypeReference<BaseRsp<List<TapdBugRsp>>>() {
                });
                return baseRsp.getData();
            } else {
                log.info("Request bugsWithNonNullTid failed. Response Code: " + response.code());
            }
        } catch (IOException e) {
           log.error("Request bugsWithNonNullTid failed.", e);
        }
        return null;
    }

    /**
     * 评论
     * @param comment
     * @param bugId
     * @return
     */
    public TapdCommentRsp comment(String comment,String bugId) {
        OkHttpClient client = getUnsafeOkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "comment="+comment);
        Request request = new Request.Builder()
                .url(traceConfig.getTapdPreFix()+"/iapi/tapd/workspace/"+traceConfig.getTapdWorkspaceId()+"/bug/"+bugId+"/comment")
                .method("POST", body)
                .addHeader("Authorization", traceConfig.getAuthorization())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                BaseRsp<TapdCommentRsp> baseRsp = JSON.parseObject(responseBody, new TypeReference<BaseRsp<TapdCommentRsp>>() {
                });
                return baseRsp.getData();
            } else {
                log.info("Request comment failed. Response Code: " + response.code());
            }
        } catch (IOException e) {
            log.error("Request comment failed.", e);
        }
        return null;
    }


    // 创建一个 OkHttpClient，禁用证书验证
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // 创建一个 TrustManager，用于禁用证书验证
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }
            } };

            // 创建 SSLContext，并设置 TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 创建 OkHttpClient，并设置 SSLContext
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // 绕过主机名验证

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
