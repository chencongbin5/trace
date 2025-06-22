package com.akuchen.trace.parse.http;

import com.akuchen.trace.parse.dto.CompletionsRequest;
import com.akuchen.trace.parse.dto.CompletionsResponse;
import com.akuchen.trace.parse.utils.ApolloUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Objects;

@Slf4j
public class ChatGPThttp {

    public static CompletionsResponse completion(CompletionsRequest completionsRequest){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(completionsRequest));
        log.info("gpt url:{}",ApolloUtils.getUrl());
        log.info("gpt code:{}",ApolloUtils.getCode());
        Request request = new Request.Builder()
                .url(ApolloUtils.getUrl()+"chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+ApolloUtils.getCode())
                .build();
        try {
            log.info("chatGpt request {}:",JSON.toJSONString(completionsRequest));
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            log.info("chatGpt response :{}",response.code());
            log.info("chatGpt response :{}",result);
            if(Objects.equals(response.code(),200)){
                CompletionsResponse completionsResponse = JSON.parseObject(result, CompletionsResponse.class);
                return  completionsResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
