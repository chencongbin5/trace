package com.akuchen.trace.parse.utils;

import com.akuchen.trace.parse.enums.HeadEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class HttpQueryUtils {

    public static Response query(String url, String bodyJson, Set<Map.Entry<String, Object>> headers) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyJson);
        Request.Builder builder = new Request.Builder();
        builder.url(url).method("POST", body);
        if(Objects.nonNull(headers)){
            headers.stream().filter(t->Optional.ofNullable(t.getValue()).isPresent())
                    .forEach(t->{
                        HeadEnum headEnum = HeadEnum.of(t.getKey());

                        String val = headEnum.convert(t.getValue().toString());
                        builder.addHeader(HeadEnum.of(t.getKey()).getHttpCommonHeader(), val);
                    });
        }

        Request request = builder.build();
        Response response = client.newCall(request).execute();
        return response;
    }
}
