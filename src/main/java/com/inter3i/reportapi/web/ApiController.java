package com.inter3i.reportapi.web;

import com.inter3i.reportapi.domain.ApiResult;
import com.inter3i.reportapi.domain.ApiResult.DimensionsAndIndicators;
import com.inter3i.reportapi.domain.QueryParams;
import com.inter3i.reportapi.service.MongoQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuguowei on 9/14/17.
 */
@RestController
@RequestMapping("/report-api")
@Slf4j
public class ApiController {
    @Autowired
    private MongoQueryService mongoQueryService;

    @CrossOrigin
    @PostMapping("/search")
    public ApiResult search(@RequestBody QueryParams query){
        log.info(query.toString());
        List<Map> mapList = mongoQueryService.stat(query);
        ApiResult result = convertMapList2ApiResult(query.getProcessedGroupBy(),mapList);
        return result;
    }

    private ApiResult convertMapList2ApiResult(String groupBy, List<Map> mapList) {
        ApiResult result = new ApiResult();
        result.setSuccess(true);
        List<DimensionsAndIndicators> daiList = new ArrayList<>(mapList.size());
        for (Map rawData : mapList) {
            // 从原始查询结果中分离出维度和指标
            String[] groupBySplit = groupBy.split(","); //维度
            Map<String, String> dimensionMap = new HashMap<>(groupBySplit.length);
            for (String dimensionKey : groupBySplit) {
                dimensionMap.put(dimensionKey, rawData.get(dimensionKey).toString());
                rawData.remove(dimensionKey);
            }

            DimensionsAndIndicators dai = new DimensionsAndIndicators();
            dai.setDimensions(dimensionMap);
            dai.setData(rawData);

            daiList.add(dai);
        }
        result.setDatas(daiList);
        return result;
    }
}
