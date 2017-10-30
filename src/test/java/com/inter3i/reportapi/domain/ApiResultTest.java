package com.inter3i.reportapi.domain;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.assertj.core.util.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * Created by zhuguowei on 9/15/17.
 */
public class ApiResultTest {
    @Test
    public void testToJsonString(){
        ApiResult result = new ApiResult();

        result.setSuccess(true);

        ApiResult.DimensionsAndIndicators dai1 = new ApiResult.DimensionsAndIndicators();
        Map<String, String> dimensions = Maps.newHashMap("screen_name","西门子");
        dimensions.put("platform", "微信");
        dai1.setDimensions(dimensions);

        Map<String, Object> data = Maps.newHashMap("post_count",31);
        data.put("reposts_count", 496);
        data.put("comments_count", 496);
        data.put("praises_count", 496);
        dai1.setData(data);

        ApiResult.DimensionsAndIndicators dai2 = new ApiResult.DimensionsAndIndicators();
        Map<String, String> dimensions2 = Maps.newHashMap("screen_name","西门子");
        dimensions2.put("platform", "微博");
        dai2.setDimensions(dimensions2);

        Map<String, Object> data2 = Maps.newHashMap("post_count",31);
        data2.put("reposts_count", 496);
        data2.put("comments_count", 496);
        data2.put("praises_count", 496);
        dai2.setData(data2);

        result.setDatas(Lists.newArrayList(dai1,dai2));

        System.out.println(JSON.toJSONString(result));
    }
}