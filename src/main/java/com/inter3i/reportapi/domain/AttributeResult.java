package com.inter3i.reportapi.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by koreyoshi on 2017/9/18.
 */
@Data
public class AttributeResult {

    private boolean success;
    private DimensionsAndQuota datas;
    private int errorCode;


    /**
     * 维度和指标
     */
    @Data
    public static class DimensionsAndQuota{
        private List<Map<String,String>> dimensions; //维度
        private List<Map<String,String>> quota; // 指标
    }
}
