package com.inter3i.reportapi.service;

import com.inter3i.reportapi.domain.QueryParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.assertj.core.util.Lists.newArrayList;

/**
 * Created by zhuguowei on 9/14/17.
 */
@RunWith(SpringRunner.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class,includeFilters = @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE,value=MongoQueryService.class))
public class MongoQueryServiceTest {
    @Autowired
    private MongoQueryService service;
    @Test
    public void officialAccountOverviewStat() throws Exception {
        List<String> whereList = newArrayList();
        whereList.add("screen_name in 西门子");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "screen_name,platform";
        String select = "count post_count,sum read_count,sum reposts_count,sum comments_count,sum praises_count,max followers_count,fh interaction_amount,fh interaction_rate";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void officialAccountOnlyFhKeyStat() throws Exception {
        List<String> whereList = newArrayList();
        whereList.add("screen_name in 西门子");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "screen_name,platform";
        String select = "fh interaction_amount,fh avg_interaction,fh interaction_rate,fh avg_read";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void volumeOverviewStat() throws Exception {
        List<String> whereList = newArrayList();
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands]";
        String select = "count volume,fh volume_ratio";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);


        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void volumeOnlyFhKeysStat() throws Exception {
        List<String> whereList = newArrayList();
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands]";
        String select = "fh volume_ratio";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);


        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void volumeTrendsStat() throws Exception {
        List<String> whereList = newArrayList();

        whereList.add("brands in 西门子,IBM");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-08-04");
        String groupBy = "created_date,[brands]";
        String select = "count volume";
        String sort = "created_date 1,brands 1";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);
        params.setSort(sort);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void volumeAndInteractionAmountStat() throws Exception {
        // 统计声量及互动量
        List<String> whereList = newArrayList();
        whereList.add("brands in 西门子,IBM");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands]";
        String select = "count volume,fh interaction_amount,sum read_count";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }

    @Test
    public void brandsSentimentVolumeStat() throws Exception {
        // 品牌健康度 统计正面声量 及 负面声量 及 占比
        List<String> whereList = newArrayList();
        whereList.add("brands in 西门子,IBM,GE");
        whereList.add("platform in 微博,微信");
//        whereList.add("sentiment in 0,1"); // 支持
//        whereList.add("sentiment gte 0");
//        whereList.add("sentiment lte 1");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands],sentiment";
        String select = "count volume,fh brands_total_volume,fh sentiment_volume_ratio";
        String sort = "brands 1,sentiment 1";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);
        params.setSort(sort);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void brandsSentimentVolumeRatioStat() throws Exception {
        // 仅统计情感声量占比
        List<String> whereList = newArrayList();
        whereList.add("brands in 西门子,IBM");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands],sentiment";
        String select = "count volume,fh sentiment_volume_ratio";
        String sort = "brands 1,sentiment 1";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);
        params.setSort(sort);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }
    @Test
    public void sortByBrandsTotalVolume() throws Exception {
        // 按复合指标：品牌总声量排序
        List<String> whereList = newArrayList();
        whereList.add("brands in 西门子,IBM,GE");
        whereList.add("platform in 微博,微信");
        whereList.add("created_date gte 2017-08-01");
        whereList.add("created_date lt 2017-09-01");
        String groupBy = "[brands],sentiment";
        String select = "count volume,fh brands_total_volume,fh sentiment_volume_ratio";
        String sort = "brands_total_volume -1,sentiment 1";
        QueryParams params = new QueryParams();
        params.setWhereList(whereList);
        params.setGroupBy(groupBy);
        params.setSelect(select);
        params.setSort(sort);

        List<Map> result = service.stat(params);
        result.forEach(System.out::println);
    }

}