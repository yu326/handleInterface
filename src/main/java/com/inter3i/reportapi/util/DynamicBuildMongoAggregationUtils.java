package com.inter3i.reportapi.util;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by zhuguowei on 9/14/17.
 */
public class DynamicBuildMongoAggregationUtils {
    private static final String UNWIND_KEY_REGEX = ".*\\[(\\w+)].*";
    public static ProjectionOperation buildProjectOperation(String groupBy, String select) {
        /**
         * project("post_count","read_count","reposts_count", "comments_count", "praises_count").and("_id.screen_name").as("screen_name")
         .and("_id.platform").as("platform").andExclude("_id")
         */

        String[] selectSplit = select.split(",");
        List<String> keys = newArrayList();
        for (String s : selectSplit) {
            String[] operAndKey = s.split(" ");
            String oper = operAndKey[0];
            String key = operAndKey[1];
            if("fh".equals(oper)){ //略过复合指标
                continue;
            }
            keys.add(key);
        }
        ProjectionOperation project = project(keys.toArray(new String[]{}));
        String[] groupBySplit = groupBy.split(",");
        for (String key : groupBySplit) {
            String name = groupBySplit.length == 1 ?"_id":"_id."+key;
            project = project.and(name).as(key);
        }
        project = project.andExclude("_id");
        return project;
    }

    public static GroupOperation buildGroupOperation(String groupBy, String select) {

        // String select = "count post_count,sum read_count,sum reposts_count,sum comments_count,sum praises_count";

        //        group("screen_name", "platform").count().as("post_count").sum("read_count").as("read_count")
        //                .sum("reposts_count").as("reposts_count").sum("comments_count").as("comments_count")
        //                .sum("praises_count").as("praises_count"),
        GroupOperation group = group(groupBy.split(","));

        String[] split = select.split(",");

        for (String s : split) {
            String[] operAndKey = s.split(" ");
            String oper = operAndKey[0];
            String key = operAndKey[1];
            if("fh".equals(oper)){ //略过复合指标
                continue;
            }
            if ("count".equals(oper)) {
                group = group.count().as(key);
            } else if ("sum".equals(oper)) {
                group = group.sum(key).as(key);
            }else if("max".equals(oper)){
                group = group.max(key).as(key);
            }

        }
        return group;
    }


    /**
     *  忽略列表属性 即unwindKey
     * @param whereList
     * @param unwindKey
     * @return
     */
    public static Criteria buildCriteria1(List<String> whereList, String unwindKey) {
        //        Criteria criteria = new Criteria().andOperator(where("screen_name").in(screenNames), where("platform").in(platforms),
        //                where("created_date").gte(beginDate), where("created_date").lt(endDate));
        List<Criteria> allCriteriaList = getCriteriaList(whereList);
        // 忽略列表属性 如[brands] 假如有的话
        List<Criteria> excludeUnwindKeyList = allCriteriaList.stream().filter(e -> !e.getKey().equals(unwindKey)).collect(toList());
        return new Criteria().andOperator(excludeUnwindKeyList.toArray(new Criteria[]{}));
    }

    public static List<Criteria> getCriteriaList(List<String> whereList) {
        List<Criteria> whereCriteriaList = newArrayList();
        for (String s : whereList) {
            String[] split = s.split(" ");
            String key = split[0];
            String oper = split[1];
            String value = split[2];

            if (key.matches(UNWIND_KEY_REGEX)) { //针对列表属性进行处理 如[brands] --> brands
                key = extractUnwindKey(key);
            }
            Object processedValue = valueProcess(value);

            Criteria where = where(key);
            if("in".equals(oper)){
                where = where.in(valueProcess2(value));
            }else if ("gte".equals(oper)){
                where = where.gte(processedValue);
            }else if("lt".equals(oper)){
                where = where.lt(processedValue);
            } else if("lte".equals(oper)){
                where = where.lte(processedValue);
            }

            whereCriteriaList.add(where);
        }
        return whereCriteriaList;
    }

    private static Object valueProcess(String rawValue) {
        //TODO: 待完善 先简单粗暴处理 凡是纯数字均转成Long型
        return rawValue.matches("[0-9]+") ? Long.valueOf(rawValue) : rawValue;
    }

    /**
     * 对逗号分隔的字符串进行处理 如果都是数字组成 由String[] --> List<Long> 否则 String[] --> List<String>
     * @param rawValue
     * @return
     */
    private static List<Object> valueProcess2(String rawValue) {
        if(!rawValue.matches("[0-9]+(,*[0-9]*)*")){
            return Arrays.asList(rawValue.split(","));
        }
        String[] split = rawValue.split(",");
        List<Object> target = new ArrayList(split.length);
        for (int i = 0; i < split.length; i++) {
            target.add(valueProcess(split[i]));
        }
        return target;
    }

    public static UnwindOperation buildUnwindOperation(String groupBy) {

        String unwindKey = getUnwindKey(groupBy);
        return unwindKey!=null ? unwind(unwindKey) : null;
    }

    public static String getUnwindKey(String groupBy) {

        String unwindKey = null;
        if(groupBy.matches(UNWIND_KEY_REGEX)) {
            unwindKey = extractUnwindKey(groupBy);
        }
        return unwindKey;
    }

    private static String extractUnwindKey(String str) {
        return str.replaceFirst(UNWIND_KEY_REGEX, "$1");
    }

    /**
     * 仅取列表属性 即unwindKey 如[brands]
     * @param whereList
     * @param unwindKey
     * @return
     */
    public static Criteria buildCriteria2(List<String> whereList,String unwindKey) {
        if(unwindKey == null){ // 不存在列表属性
            return null;
        }
        List<Criteria> allCriteriaList = getCriteriaList(whereList);
        // 仅取列表属性 如[brands] 如果where中存在的话
        List<Criteria> onlyUnwindKeyList = allCriteriaList.stream().filter(e -> e.getKey().equals(unwindKey)).collect(toList());

        return !onlyUnwindKeyList.isEmpty() ? new Criteria().andOperator(onlyUnwindKeyList.toArray(new Criteria[]{})) : null;
    }
    public static SortOperation buildSortOperation(String sort, List<String> fhKeys) {
        SortOperation sortOperation = null;
        if(!StringUtils.isEmpty(sort)){
            String[] split = sort.split(",");
            for (String s : split) {
                String[] keyAndDirection = s.split(" ");
                String key = keyAndDirection[0];
                String directionFlag = keyAndDirection[1];
                // 排除复合指标
                if(fhKeys.contains(key)){
                    continue;
                }
                Direction direction = "1".equals(directionFlag) ? Direction.ASC : Direction.DESC;
                sortOperation = sortOperation==null ? sort(direction, key) : sortOperation.and(direction, key);
            }
        }
        return sortOperation;
    }

}
