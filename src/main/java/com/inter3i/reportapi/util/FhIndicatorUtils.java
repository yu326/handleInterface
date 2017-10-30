package com.inter3i.reportapi.util;

import lombok.Data;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

/**
 * 复合指标处理工具类
 * Created by zhuguowei on 9/14/17.
 */
public class FhIndicatorUtils {
    private final static ExpressionParser parser = new SpelExpressionParser();
    private final static Pattern pattern = Pattern.compile("#\\{(\\w+)}");
    /**
     * 注入复合指标
     * @param mappedResults
     * @param key2ElMap
     */
    public static void populateFhValues(List<Map> mappedResults, Map<String, String> key2ElMap) {

        MappedResultsContext intiCalcContext = new MappedResultsContext(mappedResults);
        List<Map> mappedResultContextList = new ArrayList<>(mappedResults.size());
        for (Map mappedResult : mappedResults) {
            mappedResultContextList.add(new HashMap(mappedResult));
        }
        // 预计算
        for (Map.Entry<String, String> e : key2ElMap.entrySet()) {
            final String key = e.getKey();
            if("volume_ratio".equals(key)){ // 声量占比 需要预计算总声量
                for (Map mapContext : mappedResultContextList) {
                    String initCalcEl = "sum('volume')";
                    Object initCalcVal = getInitCalcVal(intiCalcContext, mapContext, initCalcEl);
                    mapContext.put("total_volume",initCalcVal);
                }
            }else if("brands_total_volume".equals(key)){ // 品牌总声量
                for (Map mapContext : mappedResultContextList) {
                    String initCalcEl = "sum('volume','brands',#{brands})";
                    Object initCalcVal = getInitCalcVal(intiCalcContext, mapContext, initCalcEl);
                    mapContext.put("brands_total_volume", initCalcVal);
                }
            }

        }
        for (int i = 0; i < mappedResults.size(); i++) {
            for (Map.Entry<String, String> e : key2ElMap.entrySet()) {
                String key = e.getKey();
                String el = e.getValue();
                Expression expression = parser.parseExpression(el);
                Object value = expression.getValue(mappedResultContextList.get(i));
                mappedResults.get(i).put(key, value);
            }
        }
    }

    public static Object getInitCalcVal(final MappedResultsContext intiCalcContext, final Map map, final String initCalcEl) {
        String processedInitCalcEl = processInitCalcEL(map, initCalcEl);
        return parser.parseExpression(processedInitCalcEl).getValue(intiCalcContext);
    }

    public static String processInitCalcEL(Map map, String initCalcEl) {
        List<String> variableNames = extractVariableNames(initCalcEl);
        String processedInitCalcEl = initCalcEl;
        for (String variableName : variableNames) {
            processedInitCalcEl = processedInitCalcEl.replaceFirst("#\\{" + variableName + "}", "'"+map.get(variableName).toString()+"'");
        }
        return processedInitCalcEl;
    }

    /**
     * 得到复合指标对应的el表达式
     * @param fhKeys
     * @return
     */
    public static Map<String, String> getFhKey2ElMap(List<String> fhKeys) {
        Map<String, String> key2ElMap = new HashMap<>();
        if(!fhKeys.isEmpty()){
            for (String key : fhKeys) {
                String el = null;
                if("interaction_amount".equals(key)){
                    el = "['reposts_count']+['comments_count']+['praises_count']";
                }else if ("avg_interaction".equals(key)){
                    el = "(['reposts_count']+['comments_count']+['praises_count'])/['post_count']";
                }else if("avg_read".equals(key)){
                    el = "['read_count']/['post_count']";
                }else if("interaction_rate".equals(key)){ //互动率
                    el = "['followers_count']!=null && ['followers_count']>0 ?(new java.math.BigDecimal((['reposts_count']+['comments_count']+['praises_count'])*100.0).divide(new java.math.BigDecimal(['followers_count']),2,T(java.math.RoundingMode).HALF_DOWN)):0";
                }else if("volume_ratio".equals(key)){ //声量占比
                    el = "new java.math.BigDecimal(['volume']*100).divide(new java.math.BigDecimal(['total_volume']),2,T(java.math.RoundingMode).HALF_DOWN)";
                }else if("brands_total_volume".equals(key)){ //品牌总声量
                    el = "['brands_total_volume']";
                }else if("sentiment_volume_ratio".equals(key)){ //情感声量占比
                    el = "new java.math.BigDecimal(['volume']*100).divide(new java.math.BigDecimal(['brands_total_volume']),2,T(java.math.RoundingMode).HALF_DOWN)";
                }
                key2ElMap.put(key, el);
            }
        }
        return Collections.unmodifiableMap(key2ElMap);
    }

    /**
     * 得到复合指标依赖的那些基本或复合指标
     * @param fhKeys
     * @return
     */
    public static String getDependedSelect(List<String> fhKeys) {
        StringBuilder sb = new StringBuilder();
        if(!fhKeys.isEmpty()){
            for (String key : fhKeys) {
                String select = null;
                if("interaction_amount".equals(key)){
                    select = "sum reposts_count,sum comments_count,sum praises_count";
                }else if ("avg_interaction".equals(key)){
                    select = "sum reposts_count,sum comments_count,sum praises_count,count post_count";
                }else if("avg_read".equals(key)){
                    select = "sum read_count,count post_count";
                }else if("interaction_rate".equals(key)){ //互动率
                    select = "sum reposts_count,sum comments_count,sum praises_count,max followers_count";
                }else if("volume_ratio".equals(key)){ //声量占比
                    select = "count volume";
                }else if("brands_total_volume".equals(key)){ //品牌总声量
                    select = "count volume";
                }else if("sentiment_volume_ratio".equals(key)){ //情感声量占比
                    select = "fh brands_total_volume";
                }
                sb.append(select).append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 抽取复合指标
     * @param select
     * @return
     */
    public static List<String> extractFhKeys(String select) {
        List<String> fhKeys = newArrayList();
        String[] selectSplit = select.split(",");
        for (String s : selectSplit) {
            if(s.startsWith("fh ")){
                String[] fhAndKey = s.split(" ");
                String key = fhAndKey[1];
                fhKeys.add(key);
            }
        }
        return Collections.unmodifiableList(fhKeys);
    }
    @Data
    public static class MappedResultsContext{
        public MappedResultsContext(List<Map> mapList) {
            this.mapList = mapList;
        }

        private List<Map> mapList;

        public int sum(String key){
            return mapList.stream().mapToInt(e->(int)e.get(key)).sum();
        }
        public int sum(String key,String filterKey,String filterValue){
            return mapList.stream().filter(e->e.get(filterKey).equals(filterValue)).mapToInt(e->(int)e.get(key)).sum();
        }
    }

    /**
     * 抽取动态变量
     * @param el
     * @return
     */
    private static List<String> extractVariableNames(String el){
        List<String> names = new ArrayList<>();
        Matcher matcher = pattern.matcher(el);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }

        return names;
    }

}
