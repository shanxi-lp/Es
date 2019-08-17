package es;


import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import javax.xml.ws.Response;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetConnection {
    public final static String HOST = "127.0.0.1";
    // http请求的端口是9200，客户端是9300
    public final static int PORT = 9300;

    @SuppressWarnings({"resource", "unchecked"})
    public static TransportClient getConnection() throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", "wali").build();
        // 创建client

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));

        return client;
    }

    public static void main(String[] args) throws Exception {

        TransportClient client = getConnection();
        System.out.println("client==" + client.toString());
        String id="java"+"_"+"123";
        GetResponse response=client.prepareGet("recommend","reference",id).execute().get();
        System.out.println(response.getSource());


//        SearchRequestBuilder builder = client.prepareSearch("t")
//                .setTypes("t1")
//                .setQuery(QueryBuilders.matchQuery("name", "李四"));
//        SearchResponse searchResponse = builder.get();
//        SearchHits hits = searchResponse.getHits();
//        int z = 0;
//        int c = 0;
//        int a = 0;
//        int b = 0;
//        for (SearchHit hit : hits) {
//            Map<String, Object> map = hit.getSourceAsMap();
//            System.out.println(map);
//            switch ((String) map.get("degree")) {
//                case "初级":
//                    a = a + 1;
//                    z += Integer.parseInt((String) map.get("score"));
//                    break;
//                case "中级":
//                    b = b + 1;
//                    c += Integer.parseInt((String) map.get("score"));
//                    break;
//            }
//        }
//        System.out.println("c：" + c + "," + a + "------------>" + " z:" + z + "," + b);


    }
}
