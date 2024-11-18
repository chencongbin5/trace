package com.akuchen.trace.parse.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class EsQueryHttp {

    public static final Integer SIZE=1000;
    public static List<String> queryLog(Integer amount,String tid,String appName){
        RestHighLevelClient client = restHighLevelClient();
        SearchRequest searchRequest = new SearchRequest("commerce-test-applog-"+date(amount));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        TermQueryBuilder tidBuilder = QueryBuilders.termQuery("tid.keyword", tid);
        TermQueryBuilder appBuilder = QueryBuilders.termQuery("project_name.keyword", appName);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(tidBuilder).must(appBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        //设置一个可选的超时，控制允许搜索的时间
        searchSourceBuilder.timeout(new TimeValue(10, TimeUnit.SECONDS));
        SortBuilder sortBuilder = new FieldSortBuilder("@timestamp").order(SortOrder.ASC);
        searchSourceBuilder.sort(sortBuilder);
        //call_chain 拼接msg用的
        searchSourceBuilder.fetchSource(new String[]{"project_name","msg","call_chain","thread_id"}, null);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(SIZE);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;

        try {
            log.info("[Elasticsearch] DSL >>> {}", searchSourceBuilder.toString());
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            log.info("[Elasticsearch] search success. costTime={},  totalShards={}, successfulShards={}, failedShards={}", searchResponse.getTook(),  searchResponse.getTotalShards(), searchResponse.getSuccessfulShards(), searchResponse.getFailedShards());
        } catch (Exception e) {
            log.error("searchDataPage error", e);
            return Collections.emptyList();
        }
        //解析日志
        SearchHit[] hits = searchResponse.getHits().getHits();
        if(Objects.equals(SIZE,hits.length)){
            log.info("查询溢出,数据可能不全,如有需要 请调整size的值,查询size{},结果集{}",SIZE,hits.length);
            return Collections.emptyList();
        }
        //整理结果集, 我只需要message
        List<String> messages = Arrays.stream(hits).map(t -> t.getSourceAsMap().get("thread_id").toString()+"|"+t.getSourceAsMap().get("msg").toString()+t.getSourceAsMap().get("call_chain")).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(messages)){
            log.info("查询结果集为空");
            return Collections.emptyList();
        }
        return messages;
    }

    /**
     * 0 今天
     * 1 明天
     * -1 昨天
     * @param amount
     * @return
     */
    private static String date(int amount){
        Date date = DateUtils.addDays(new Date(), amount);
        return DateFormatUtils.format(date,"yyyy.MM.dd");
    }

    private static RestHighLevelClient restHighLevelClient() {
        String host="192.168.16.125";
        Integer port=31437;
        String username="elastic";
        String password="WVx5060a7cWr1GLY93m0Baa6";
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port));
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder = builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        return restHighLevelClient;
    }

    public static void main(String[] args) {
        List<String> strings = EsQueryHttp.queryLog(0,
                                                           "a775a6fd294a4db889c2bde94a95d727.215548.17198207403670017",
                                                           "order-service");
        System.out.println(strings);
    }
}
