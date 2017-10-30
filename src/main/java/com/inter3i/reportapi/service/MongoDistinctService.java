package com.inter3i.reportapi.service;

import com.alibaba.fastjson.JSONArray;
import com.inter3i.reportapi.util.ValidateUtils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by koreyoshi on 2017/9/21.
 */
@Service
public class MongoDistinctService {

    @Autowired
    private MongoOperations mongoOperations;

    //维度字段映射
    final static HashMap<String, String> Dimensions_FIELD_Mapping = new HashMap<String, String>();

    static {
        Dimensions_FIELD_Mapping.put("screen_name", "screen_name"); //品牌
        Dimensions_FIELD_Mapping.put("platform", "platform");    //平台
    }


    /**
     * 接收前台传过来的json串，去mongo表中distinct查询出其对应字段的所有值
     *
     * @param param 字符串
     * @return
     */
    public List<Map<String, List<Object>>> handleDistinctField(String param) {
        JSONArray params = (JSONArray) JSONArray.parse(param);
        Iterator<Object> it = params.iterator();

        List<Map<String, List<Object>>> responseArr = new ArrayList<Map<String, List<Object>>>();
        DBObject query = Query.query(Criteria.where("is_official").is(1)).getQueryObject();

        while (it.hasNext()) {
            String distinctKey = null;
            String paramValue = (String) it.next();
            if (!ValidateUtils.isNullOrEmpt(Dimensions_FIELD_Mapping.get(paramValue))) {
                distinctKey = Dimensions_FIELD_Mapping.get(paramValue);
            } else {
                break;
            }
            Map<String, List<Object>> map = new HashMap();
            DBCollection dbCollection = mongoOperations.getCollection("test_v3");
            List res = dbCollection.distinct(distinctKey, query);
            map.put(paramValue, res);
            responseArr.add(map);
        }

        return responseArr;
    }
}
