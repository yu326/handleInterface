package com.inter3i.reportapi.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Api调用返回结果
 * - success
 * - datas
 *     - dimensions
 *          - key : value
 *     - data
 *          - key : value
 * Created by zhuguowei on 9/15/17.
 */
@Data
public class ApiResult {
    private boolean success = true;

    private List<DimensionsAndIndicators> datas;

    /**
     * 维度和指标
     */
    @Data
    public static class DimensionsAndIndicators{
        private Map<String,String> dimensions; //维度
        private Map<String,Object> data; // 指标
    }
}
