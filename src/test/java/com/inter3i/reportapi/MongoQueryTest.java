package com.inter3i.reportapi;

import com.mongodb.DBCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static com.inter3i.reportapi.util.DynamicBuildMongoAggregationUtils.*;
import static com.inter3i.reportapi.util.FhIndicatorUtils.*;
import static org.assertj.core.util.Lists.newArrayList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by zhuguowei on 9/13/17.
 */
@RunWith(SpringRunner.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class MongoQueryTest {
    @Autowired
    private MongoOperations mongoOperations;

    @Test
    public void getaaa(){
        DBCollection dbCollection = mongoOperations.getCollection("testSegment");
        List res= dbCollection.distinct("cacheDataTime");
        System.out.println(res);
    }

    @Test
    public void officialAccountOverview(){
        /**
         * # 官方账号总览统计
         * db.v3_test_data.aggregate(
         {$match: {"screen_name":{$in:['西门子']}, "platform":{$in:['微博','微信']},"created_date":{$gte:'2017-08-01',$lt:'2017-09-01'} }},
         {$group: {_id:{screen_name:"$screen_name",platform:"$platform"}, post_count:{$sum:1},read_count:{$sum:"$read_count"},reposts_count:{$sum:"$reposts_count"},comments_count:{$sum:"$comments_count"},praises_count:{$sum:"$praises_count"} } },
         {$project: {_id:0, screen_name:"$_id.screen_name", platform:"$_id.platform", post_count:1,read_count:1,reposts_count:1,comments_count:1,praises_count:1} }
         )
         */
        String[] screenNames = {"西门子"};
        String[] platforms = {"微博","微信"};
        String beginDate = "2017-08-01";
        String endDate = "2017-09-01";
        Criteria criteria = new Criteria().andOperator(where("screen_name").in(screenNames), where("platform").in(platforms),
                where("created_date").gte(beginDate), where("created_date").lt(endDate));
        Aggregation aggregation = newAggregation(match(criteria),
                group("screen_name", "platform").count().as("post_count").sum("read_count").as("read_count")
                        .sum("reposts_count").as("reposts_count").sum("comments_count").as("comments_count")
                        .sum("praises_count").as("praises_count").max("followers_count").as("followers_count"),
                project("post_count","read_count","reposts_count", "comments_count", "praises_count","followers_count").and("_id.screen_name").as("screen_name")
                        .and("_id.platform").as("platform").andExclude("_id")
        );
        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);
    }
    @Test
    public void dynamicallyOfficialAccountOverview() {
        /**
         * where :
         - screen_name in []
         - platform in []
         - created_date gte ?
         - created_date lt ?

         group by:
         - screen_name platform

         select:
         - post_count,read_count,reposts_count,comments_count,praises_count
         - count(post_count),sum(read_count),sum(reposts_count),sum(comments_count),sum(praises_count)
         */
        List<String> whereList = newArrayList();
        whereList.add("screen_name in 西门子,IBM");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        Criteria criteria = buildCriteria1(whereList,null);
        String groupBy = "screen_name,platform";
        String select = "count post_count,sum read_count,sum reposts_count,sum comments_count,sum praises_count,fh interaction_amount,fh avg_interaction,fh avg_read";
        GroupOperation group = buildGroupOperation(groupBy, select);

        ProjectionOperation project = buildProjectOperation(groupBy, select);


        Aggregation aggregation = newAggregation(match(criteria), group, project);
        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        List<Map> mappedResults = result.getMappedResults();
        mappedResults.forEach(System.out::println);

        // 支持复合指标
        List<String> fhKeys = extractFhKeys(select);
        Map<String, String> key2ElMap = getFhKey2ElMap(fhKeys);

        // 基于el表达式 计算复合指标
        populateFhValues(mappedResults, key2ElMap);

        mappedResults.forEach(System.out::println);
    }

    @Test
    public void dynamicallyOfficialAccountOverview_all_platform() {

        List<String> whereList = newArrayList();
        whereList.add("screen_name in 西门子");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        Criteria criteria = buildCriteria1(whereList,null);
        String groupBy = "screen_name,platform";
        String select = "count post_count,sum read_count,sum reposts_count,sum comments_count,sum praises_count";
        GroupOperation group = buildGroupOperation(groupBy, select);

        ProjectionOperation project = buildProjectOperation(groupBy, select);

        Aggregation aggregation = newAggregation(match(criteria), group, project);
        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);

    }
    @Test
    public void dynamicallyOfficialAccountOverview_only_post_count() {

        List<String> whereList = newArrayList();
        whereList.add("screen_name in 西门子");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        Criteria criteria = buildCriteria1(whereList,null);
        String groupBy = "screen_name";
        String select = "count post_count";
        GroupOperation group = buildGroupOperation(groupBy, select);

        ProjectionOperation project = buildProjectOperation(groupBy, select);

        Aggregation aggregation = newAggregation(match(criteria), group, project);
        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);

    }


    @Test
    public void brandVolumeOverview(){
        /**
         * # 声量统计
         * db.v3_test_data.aggregate(
         {$match: {"platform":{$in:['微博']}, "created_date":{$gte:'2017-08-01',$lt:'2017-09-01'} } },
         {$unwind: "$brands"},
         {$group: {"_id":"$brands", count:{$sum:1}} },
         {$project: {"_id":0, brand:"$_id",count:1} }
         )
         */
        String[] platforms = {"微博","微信"};
        String beginDate = "2017-08-01";
        String endDate = "2017-09-01";
        Criteria criteria = new Criteria().andOperator(where("platform").in(platforms), where("created_date").gte(beginDate), where("created_date").lt(endDate));
        UnwindOperation unwindOperation = unwind("brands");

        MatchOperation match = match(criteria);
        GroupOperation groupOperation = group("brands").count().as("volume");
        ProjectionOperation projectionOperation = project("volume").and("_id").as("brands").andExclude("_id");

        List<AggregationOperation> operationList = newArrayList(match,unwindOperation,groupOperation,projectionOperation);
        Aggregation aggregation = newAggregation(operationList);

        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);
    }
    @Test
    public void specifiedBrandVolume(){
        /**
         * # 指定品牌的声量统计
         * db.v3_test_data.aggregate(
         {$match: {"platform":{$in:['微博']}, "created_date":{$gte:'2017-08-01',$lt:'2017-09-01'} } },
         {$unwind: "$brands"},
         {$match: {"brands":{$in:['西门子','ABB']}} },
         {$group: {"_id":"$brands", count:{$sum:1}} },
         {$project: {"_id":0, brand:"$_id",count:1} }
         )
         */
        String[] brands = {"西门子","ABB"};
        String[] platforms = {"微博","微信"};
        String beginDate = "2017-08-01";
        String endDate = "2017-09-01";
        Criteria criteria = new Criteria().andOperator(where("platform").in(platforms), where("created_date").gte(beginDate), where("created_date").lt(endDate));
        MatchOperation match1 = match(criteria);
        UnwindOperation unwindOperation = unwind("brands");
        Criteria criteria2 = new Criteria().andOperator(where("brands").in(brands));
        MatchOperation match2 = match(criteria2);
        GroupOperation groupOperation = group("brands").count().as("volume");
        ProjectionOperation projectionOperation = project("volume").and("_id").as("brands").andExclude("_id");

        List<AggregationOperation> operationList = newArrayList(match1,unwindOperation,match2,groupOperation,projectionOperation);
        Aggregation aggregation = newAggregation(operationList);

        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);
    }
    @Test
    public void brandVolumeTrends(){
        /**
         * # 品牌声量传播趋势
         * db.v3_test_data.aggregate(
         {$match: {"platform":{$in:['微博']}, "created_date":{$gte:'2017-08-01',$lt:'2017-09-01'} } },
         {$unwind: "$brands"},
         {$match: {"brands":{$in:['西门子']}} },
         {$group: {"_id":{brand:"$brands",date:"$created_date"}, count:{$sum:1}} },
         {$project: {"_id":0, brand:"$_id.brand",date:"$_id.date",count:1} },
         {$sort:{date:1,brand:1}}
         )
         */
        String[] brands = {"西门子","ABB"};
        String[] platforms = {"微博"};
        String beginDate = "2017-08-01";
        String endDate = "2017-08-03";
        Criteria criteria = new Criteria().andOperator(where("platform").in(platforms), where("created_date").gte(beginDate), where("created_date").lt(endDate));
        MatchOperation match1 = match(criteria);
        UnwindOperation unwindOperation = unwind("brands");
        Criteria criteria2 = new Criteria().andOperator(where("brands").in(brands));
        MatchOperation match2 = match(criteria2);
        GroupOperation groupOperation = group("brands","created_date").count().as("volume");
        ProjectionOperation projectionOperation = project("volume").and("_id.brands").as("brands").and("_id.created_date").as("created_date").andExclude("_id");
        SortOperation sort = sort(Sort.Direction.ASC, "created_date").and(Sort.Direction.ASC,"brands");
        List<AggregationOperation> operationList = newArrayList(match1,unwindOperation,match2,groupOperation,projectionOperation,sort);
        Aggregation aggregation = newAggregation(operationList);

        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);
        result.getMappedResults().forEach(System.out::println);
    }






}
