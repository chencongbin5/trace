//package com.akuchen.trace.parse.mock;
//
//import com.akuchen.trace.parse.builder.MethodMockBuilder;
//import com.akuchen.trace.parse.dto.CodeInfo;
//import com.akuchen.trace.parse.dto.EsResponse;
//import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;
//import com.akuchen.trace.parse.dto.TemplateReq;
//import com.akuchen.trace.parse.utils.ValUtils;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.time.DateFormatUtils;
//import org.apache.commons.lang3.time.DateUtils;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.TermQueryBuilder;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.sort.FieldSortBuilder;
//import org.elasticsearch.search.sort.SortBuilder;
//import org.elasticsearch.search.sort.SortOrder;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * 正式环境mock
// */
//@Slf4j
//public class MockEnvIdc {
//
//    //线上不能直接调用es 接口,有白名单限制,  折中办法 直接打开线上的分布式日志 F12  拷贝
//    //https://kibana.akuchen.com/app/kibana#/discover?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-5h,to:now))&_a=(columns:!(message,project_name),filters:!(('$state':(store:appState),meta:(alias:!n,disabled:!f,index:f02d22a0-1791-11ed-a521-d37dc0ca127c,key:project_name,negate:!f,params:(query:calculation-service),type:phrase,value:calculation-service),query:(match:(project_name:(query:calculation-service,type:phrase))))),index:f02d22a0-1791-11ed-a521-d37dc0ca127c,interval:auto,query:(language:kuery,query:''),sort:!(!('@timestamp',desc)))
////    private String cookie="sid=Fe26.2**149d4c930cc77f1233406bdacd67e2539d35c7e42219e2b2103172be42f244a3*jICYEkHOoxd9i8KI1GWIig*PeTWjbhlhSOhYvTaDD3jYKrsU_fdnmng0Q7fQXik4LKeEv8CghP8eu3eOOmbUs8cNvgxU2wAVlziEecLZb5anFxPMtIXm2UGVP-1mwLyPSM3YUiuMVmoBPqLt7PL7d2zHKyoWx948iZ4cKSlc1fLXg**203309005074d5493ddf3342ad9e7e3cbbecbf17a78a8c5c7abc33b27f4692af*U06g7_31U0dFFqnBNncXOkX7AyJ2MXstELJxRp2Ey8Q";
////    private String tid="d5e8d96df52c4f759dfeb8066ca858df.186.16727325550455003";
////    private String appName="calculation-service";
////    private Integer day=0;//昨天-1  今天0
//
//
//    public static void queryLogAndCreateClassFile(QueryLogAndCreateClassFileReq req) throws IOException {
//        File logFile = new File(req.getOutFilePath() + "\\tid.log");
//        File mockStart = new File(req.getOutFilePath() + "\\IdcMock.java");
//        log.info("0 初始化");
//        FileUtils.deleteQuietly(logFile);
//        FileUtils.deleteQuietly(mockStart);
//        log.info("1 查日志");
//        List<String> messages = queryLog(req.getDay(),req.getTid(),req.getCookie());
//        log.info("1 查日志 行数:{}", messages.size());
//        if (CollectionUtils.isEmpty(messages)) {
//            log.info("没有日志");
//            return;
//        }
//        FileUtils.writeLines(logFile, messages);
//        log.info("变更日志格式,使得解析逻辑统一");
//        messages = ValUtils.replace(messages);
//        messages = ValUtils.modifyLogClassName(messages);
//
//        log.info("2 生成mock代码");
//        CodeInfo code = MethodMockBuilder.start(messages);
//        log.info("3 生成.java代码");
//        String s = req.getOutFilePath().replaceAll("\\\\|/", ".");
//        String packageName = s.substring(s.indexOf("test.java") + 10);
//        String resut = MethodMockBuilder.template(TemplateReq.builder()
//                 .className("IdcMock")
//                .code(code)
//                .packageName(packageName)
//                .env("test")
//                .mainClass(req.getMainClass())
//                .build());
//        FileUtils.write(mockStart, resut);
//
//        log.info("4 ------------------success------------------------------------");
//
//    }
//
//
//    public static List<String> queryLog(Integer amount,String tid,String cookie) {
//
//        SearchRequest searchRequest = new SearchRequest("sre-ec-elastisearch:logstash-ec-applog-calculation-service-" + date(amount));
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//
//        TermQueryBuilder tidBuilder = QueryBuilders.termQuery("TID.keyword", tid);
//
//        searchSourceBuilder.query(tidBuilder);
//        //设置一个可选的超时，控制允许搜索的时间
//        searchSourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
//        SortBuilder sortBuilder = new FieldSortBuilder("@timestamp").order(SortOrder.ASC);
//        searchSourceBuilder.sort(sortBuilder);
//        searchSourceBuilder.fetchSource(new String[]{"project_name", "message"}, null);
//        searchSourceBuilder.from(0);
//        searchSourceBuilder.size(200);
//        searchRequest.source(searchSourceBuilder);
//
//        EsResponse searchResponse = null;
//        try {
//            log.info("[Elasticsearch] DSL >>> {}", searchSourceBuilder.toString());
//            OkHttpClient client = new OkHttpClient().newBuilder()
//                    .build();
//            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody body = RequestBody.create(mediaType, searchSourceBuilder.toString());
//            Request request = new Request.Builder()
//                    .url("https://kibana.akuchen.com/api/console/proxy?path=%2Fsre-ec-elastisearch%3Alogstash-ec-applog-calculation-service-" + date(amount) + "%2F_search&method=POST")
//                    .method("POST", body)
//                    .addHeader("kbn-version", "7.4.2")
//                    .addHeader("Content-Type", "application/json")
//                    .addHeader("Cookie", cookie)
//                    .build();
//            Response response = client.newCall(request).execute();
//            searchResponse = JSON.parseObject(response.body().string(), new TypeReference<EsResponse>() {});
//        } catch (IOException e) {
//            log.error("searchDataPage error", e);
//        }
//        //解析日志
//        List<EsResponse.Detail> hits = searchResponse.getHits().getHits();
//        if (searchResponse.getHits().getTotal().getValue() != hits.size()) {
//            log.info("查询结果集小于总记录数,请调整size的值");
//            return Collections.emptyList();
//        }
//        //整理结果集, 我只需要message
//        List<String> messages = hits.stream().map(t -> t.get_source().getMessage()).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(messages)) {
//            log.info("查询结果集为空");
//            return Collections.emptyList();
//        }
//        return messages;
//    }
//
//    /**
//     * 0 今天
//     * 1 明天
//     * -1 昨天
//     *
//     * @param amount
//     * @return
//     */
//    private static String date(int amount) {
//        Date date = DateUtils.addDays(new Date(), amount);
//        return DateFormatUtils.format(date, "yyyy.MM.dd");
//    }
//
//
//}
