package com.inter3i.reportapi.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inter3i.reportapi.dao.SubjectAttributeMapper;
import com.inter3i.reportapi.domain.AttributeResult;
import com.inter3i.reportapi.domain.QueryParams;
import com.inter3i.reportapi.entity.SubjectAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by koreyoshi on 2017/9/18.
 */
@RestController
@RequestMapping("/Attribute-api")
@Slf4j
public class AttributeController {
    @Autowired
    private SubjectAttributeMapper subjectAttributeMapper;

    final static HashMap<String, Integer> CHARTTABLES = new HashMap();

    static {
        CHARTTABLES.put("table", 1);
        CHARTTABLES.put("horbar", 2);
        CHARTTABLES.put("wordCloud", 3);
        CHARTTABLES.put("verbar", 4);
        CHARTTABLES.put("line", 5);
        CHARTTABLES.put("scatte", 6);
        CHARTTABLES.put("scatterfn", 7);
        CHARTTABLES.put("graph", 8);
    }

    @CrossOrigin
    @RequestMapping("/getData")
    public AttributeResult getData(@RequestParam("subId") int subId, @RequestParam("chartType") String chart) {

        AttributeResult result = new AttributeResult();

        int charType = 0;
        if (!CHARTTABLES.containsKey(chart)) {
            result.setSuccess(false);
            result.setErrorCode(0);
            return result;
        } else {
            charType = CHARTTABLES.get(chart);
        }

        SubjectAttribute res = subjectAttributeMapper.findById(subId, charType);
        if (res == null) {
            result.setSuccess(false);
            result.setErrorCode(1);
        } else {
            result = convertString2AttributeResult(res);
        }
        return result;
    }

    public AttributeResult convertString2AttributeResult(SubjectAttribute data) {
        AttributeResult attributeResult = new AttributeResult();

        attributeResult.setSuccess(true);

        AttributeResult.DimensionsAndQuota dimensionsAndQuota = new AttributeResult.DimensionsAndQuota();
        List<AttributeResult.DimensionsAndQuota> daiList = new ArrayList<>();


        //维度
        JSONObject DimensionArr = (JSONObject) JSONArray.parse(data.getDimension());
        ArrayList DimensionList = new ArrayList();


        for (Map.Entry<String, Object> entry : DimensionArr.entrySet()) {
            HashMap oneDimension = new HashMap();
            oneDimension.put("name", entry.getKey());
            oneDimension.put("text", entry.getValue());
            DimensionList.add(oneDimension);

        }
        dimensionsAndQuota.setDimensions(DimensionList);
        //指标
        JSONObject QuotaArr = (JSONObject) JSONArray.parse(data.getQuota());
        ArrayList QuotaList = new ArrayList();

        for (Map.Entry<String, Object> entry : QuotaArr.entrySet()) {
            HashMap oneQuota = new HashMap();
            oneQuota.put("name", entry.getKey());
            oneQuota.put("text", entry.getValue());
            QuotaList.add(oneQuota);
        }
        dimensionsAndQuota.setQuota(QuotaList);
        attributeResult.setDatas(dimensionsAndQuota);

        return attributeResult;
    }
}
