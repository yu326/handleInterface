package com.inter3i.reportapi.domain;

import lombok.Data;

import java.util.List;

/**
 * 动态查询参数
 * where :
 *- screen_name in []
 *- platform in []
 *- created_date gte ?
 *- created_date lt ?

 *group by:
 *- screen_name platform

 *select:
 *- count(post_count),sum(read_count),sum(reposts_count),sum(comments_count),sum(praises_count)
 *
 * sort
 * - created_date 1,brands 1

 * Created by zhuguowei on 9/14/17.
 */
@Data
public class QueryParams {
    private List<String> whereList;
    private String groupBy;
    private String select;
    private String sort;


    /**
     * 返回处理后的groupBy
     * - 如去除数组的标志 [brands] --> brands
     * @return
     */
    public String getProcessedGroupBy(){
        return groupBy.replaceAll("[\\[\\]]", "");
    }

}
