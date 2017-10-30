package com.inter3i.reportapi.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by koreyoshi on 2017/9/21.
 */
@Data
public class AttributeSelectionResult {
    private boolean success;
    private List<Map<String, List<Object>>> datas;
    //    private Map<String,List<Object>> aaa;
    private Integer errorCode;



}
