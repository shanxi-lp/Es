package es;


import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class type {

    /** {
             * index:recommend
                * type:reference
                * id:userid+project
                * userid
                * netname：网名
                * avatar :头像
                * project：科目
                * score：分数
                * 都是text类型的
                * 这是我创建的es
                * }
     */
    /**
     * 这个方法是算用户某一科的最终得分
     */
    public static double lastscore(double averagesimple, double avergemedium, double averagediffficulty) {
        double weight[] = new double[]{1, 3, 6};
        double num = 0.0;
        for (int i = 0; i < 3; i++) {
            num = num + weight[i];
        }
        System.out.println(num);
        double lastscore = averagesimple * (weight[0] / num) + avergemedium * (weight[1] / num) + averagediffficulty * (weight[2] / num);
        System.out.println(lastscore);
        return lastscore;
    }

    /**
     *获取该用户最后一次做题做的是什么类型的
     * @param userId
     * @return
     */
    public static String project(String userId) {
        TransportClient client = null;
        try {
            client = GetConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SearchRequestBuilder builder=client.prepareSearch("t")
                .setTypes("t1")
                .setSize(10000)
                .setQuery(QueryBuilders.matchQuery("userId",userId)).addSort("date",SortOrder.DESC);
        SearchResponse searchResponse=builder.get();
        SearchHits hits=searchResponse.getHits();
        Map<String,Object> map1=hits.getAt(0).getSourceAsMap();
        return (String) map1.get("project");
    }

    /**
     * 推荐好友
     * @param userId
     */
    public static void recommend(String userId) throws ExecutionException, InterruptedException {
        TransportClient client = null;
        try {
            client = GetConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
         String project=project(userId);
        SearchRequestBuilder mybuilder = client.prepareSearch("recommend")
                .setTypes("reference")
                .setQuery(QueryBuilders.matchQuery("userId", userId))
                .setQuery(QueryBuilders.matchQuery("project", project));
        SearchResponse mysearchResponse = mybuilder.get();
        SearchHits myhits = mysearchResponse.getHits();

        double score = 0.0;

            for (SearchHit hit : myhits) {
                Map<String, Object> map = hit.getSourceAsMap();
                score = (double) map.get("score");
            }
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("project", project))
                .must(QueryBuilders.rangeQuery("score").gte(score-5).lte(score+5));
        SearchRequestBuilder Other = client.prepareSearch("recommend")
                .setTypes("reference")
                .setQuery(boolQueryBuilder)
                ;

        SearchResponse othersearchResponse = Other.get();
        SearchHits otherhits = othersearchResponse.getHits();
        Map <String,Double> hashMap= new HashMap<>();
        for (SearchHit hit : otherhits) {
            Map<String, Object> map = hit.getSourceAsMap();
            if (!map.get("userId").equals(userId)) {
                double sub = ((double) map.get("score")) - score;
                if (sub < 0) {
                    sub = -sub;
                }
                hashMap.put((String) map.get("userId"), sub);
            }
        }
        if (hashMap.size()>0) {
            Map<String, Double> endmap = sort(hashMap);
            List<Map.Entry<String, Double>> list = new ArrayList<>(endmap.entrySet());
            List<String> arrayList = new ArrayList<>();
            for (Map.Entry<String, Double> mapping : list) {
                arrayList.add(mapping.getKey());
            }
            for (int i = 0; i < arrayList.size(); i++) {
                String id = project + "_" + arrayList.get(i);
                GetResponse response = client.prepareGet("recommend", "reference", id).execute().get();
                System.out.println(response.getSource());
                if (i == 10) {
                    break;
                }
            }
        }else{
            System.out.println("没有好友可以推荐");
        }
    }

    /**
     * 对map进行排序 从而把分数进行排序
     * @param hashMap
     * @return
     */

    public static Map<String,Double> sort(Map<String ,Double> hashMap){
        List<Map.Entry<String ,Double>> list=new ArrayList<>(hashMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        Map<String,Double> hashmap=new TreeMap<>();
        for(Map.Entry<String,Double> mapping:list){
            hashmap.put(mapping.getKey(),mapping.getValue());
        }
        return hashmap;
    }

    /**
     * 这里可以定个接口 连接题库 只要有新的数据传过来 就修改我这边的数据
     *      *从es中查询
     *       需要修改的：userid project  要和传过来的数据的名字相同以便好看
     * @param userId
     * @param project
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void select(String userId, String project) throws IOException, ExecutionException, InterruptedException {
        TransportClient client = null;
        try {
            client = GetConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int simple = 0;
        int medium = 0;
        int difficulty = 0;
        int simplesize = 0;
        int mediumsize = 0;
        int difficultysize = 0;
        /**
         * 寻要修改的参数：t、t1、"name"、"project"
         *  下面的size必须得自己设置
         */
        assert client != null;
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("userId", userId))
                .must(QueryBuilders.matchQuery("project", project));
        SearchRequestBuilder builder = client.prepareSearch("t")
                .setTypes("t1")
                .setSize(10000)
                .setQuery(boolQueryBuilder);
        SearchResponse searchResponse = builder.get();
        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            System.out.println(map);
            switch ((String) map.get("degree")) {
                case "初级":
                    simplesize = simplesize + 1;
                    simple += Integer.parseInt((String) map.get("score"));
                    break;
                case "中级":
                    mediumsize = mediumsize + 1;
                    medium += Integer.parseInt((String) map.get("score"));
                    break;
                case "高级":
                    difficultysize = difficultysize + 1;
                    difficulty += Integer.parseInt((String) map.get("score"));
                    break;
            }
        }
        System.out.println(simplesize);
        /**
         if是用来判断是否够评分标准
         */
        if (simplesize >= 5 && mediumsize >= 5 && difficultysize >= 5) {
            /**
             * lastscore表示该用户这个科目最终的得分
             */
            double lastscore = lastscore((double) simple / simplesize, (double) medium / mediumsize, (double) difficulty / difficultysize);
            /**
             这里是修改该用户对应该科目的分数
             下面的netname 和avatar 需要拿数据
             */
            String id = project + "_" + userId;
            IndexRequest indexRequest = new IndexRequest("recommend", "reference", id)
                    .source(
                            XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("userId",userId)
                                    .field("netname","netname")
                                    .field("avatar","avatar")
                                    .field("project", project)
                                    .field("score", lastscore)
                                    .endObject()
                    );
            /**
             * 这个是修改该科目的最终得分
             */
            UpdateRequest updateRequest = new UpdateRequest("recommend", "reference", id)
                    .doc(
                            XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("score", lastscore)
                                    .endObject()
                    ).upsert(indexRequest);
            UpdateResponse updateResponse = client.update(updateRequest).get();
            System.out.println(updateResponse.status());//为了测试是否更新成功
        }else {
            System.out.println("没有资格！！！！！");
        }
    }

    /**
     * 更新网名
     * 第一步：先得到修改网名这个用户总共做了哪几科的题
     * 第二部：然后在最终得分的索引里根据ID查出并更新他们的网名
     * @param userId
     * @param netname
     */
    public static void updatanetname(String userId,String netname) throws IOException, ExecutionException, InterruptedException {
        TransportClient client = null;
        try {
            client = GetConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List <String>projectlist=new LinkedList();
        /**
         * 第一步
         */
    SearchRequestBuilder builder = client.prepareSearch("t")
            .setTypes("t1")
            .setQuery(QueryBuilders.matchQuery("userId", userId))
            ;
    SearchResponse searchResponse = builder.get();
    SearchHits hits = searchResponse.getHits();
        for(SearchHit hit:hits)
    {
        Map<String, Object> map = hit.getSourceAsMap();
        projectlist.add((String) map.get("project"));
    }
        /**
         * 第二步
         */
        for (int i = 0; i <projectlist.size() ; i++) {
            String id=projectlist.get(i)+"_"+userId;
            UpdateRequest updateRequest = new UpdateRequest("recommend", "reference", id)
                    .doc(
                            XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("netname", netname)
                                    .endObject()
                    );
            UpdateResponse updateResponse = client.update(updateRequest).get();
            System.out.println(updateResponse.status());
        }

}
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException, ParseException {

//        // select("36","java");
//        recommend("36");
//       // updatanetname("36","李四");
        project("36");
    }
}

