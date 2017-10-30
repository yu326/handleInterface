package com.inter3i.reportapi;

import org.assertj.core.util.Maps;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.util.Maps.newHashMap;
import static org.junit.Assert.assertEquals;

/**
 * Created by zhuguowei on 9/14/17.
 */
public class SpelTest {
    @Test
    public void parseMap(){
        ExpressionParser parser = new SpelExpressionParser();

        Map<String, Integer> map = new HashMap<>();
        map.put("reposts_count", 3);
        map.put("comments_count", 8);
        map.put("praises_count", 12);
        map.put("post_count", 3);

        String interactionEl = "['reposts_count']+['comments_count']+['praises_count']";
        Expression expression = parser.parseExpression(interactionEl);
        int interactionCount = expression.getValue(map,Integer.class);
        assertEquals(23, interactionCount);

        String avgInteractionEl = "(['reposts_count']+['comments_count']+['praises_count'])/['post_count']";
        expression = parser.parseExpression(avgInteractionEl);
        int avgInteractionCount = expression.getValue(map,Integer.class);
        assertEquals(7, avgInteractionCount);

        // 判空
//        String interactionRateEl = "['followers_count']!=null?((['reposts_count']+['comments_count']+['praises_count'])*100.0/['followers_count']):0";
        String interactionRateEl = "['followers_count']!=null && ['followers_count']>0?(new java.math.BigDecimal((['reposts_count']+['comments_count']+['praises_count'])*100.0).divide(new java.math.BigDecimal(['followers_count']),2,T(java.math.RoundingMode).HALF_DOWN)):0";
        expression = parser.parseExpression(interactionRateEl);
        double interactionRate = expression.getValue(map,Double.class);
        assertEquals(0,interactionRate,0);

        map.put("followers_count",0);
        interactionRate = expression.getValue(map,Double.class);
        assertEquals(0,interactionRate,0);

        map.put("followers_count",3000);
        interactionRate = expression.getValue(map,Double.class);
        assertEquals(0.77,interactionRate,0);


    }
    @Test
    public void parseBigDecimal(){
        ExpressionParser parser = new SpelExpressionParser();
//        String el = "java.math.BigDecimal.valueOf('3000')"; // Property or field 'java' cannot be found on null
        String el = "T(java.math.BigDecimal).valueOf(23*100.0)";
//        String el = "new java.math.BigDecimal(23)";

        Double value = parser.parseExpression(el).getValue(Double.class);
        System.out.println(value);

        el = "new java.math.BigDecimal(23*100).divide(new java.math.BigDecimal(3000), 2, T(java.math.RoundingMode).HALF_DOWN)";
        value = parser.parseExpression(el).getValue(Double.class);
        System.out.println(value);

        Map<String, Integer> map = newHashMap("volume", 430);

//        ['volume']/#{total_volume}
        BigDecimal divide = new BigDecimal(430 * 100).divide(new BigDecimal(821), 2, RoundingMode.HALF_DOWN);
        System.out.println(divide);

    }
    @Test
    public void testParseConstants(){
        String el = "['volume'] / 821.0";
        Map<String, Integer> map = newHashMap("volume", 430);

        ExpressionParser parser = new SpelExpressionParser();
        Double value = parser.parseExpression(el).getValue(map,Double.class);
        System.out.println(value);

    }

    @Test
    @Ignore
    public void testJava8El(){
        // 不支持Java8
        //int totalVolume = mappedResults.stream().mapToInt(m -> (int)m.get("volume")).sum();
        String el = ".stream().mapToInt(m -> (int)m.get(\"volume\")).sum()";
        ArrayList<Map<String, Integer>> result = newArrayList(newHashMap("volume", 100), newHashMap("volume", 200));
        ExpressionParser parser = new SpelExpressionParser();
        Integer value = parser.parseExpression(el).getValue(result, Integer.class);
        System.out.println(value);

    }
    @Test
    public void testCallStaticMethodWithParameters(){
        List<Map> mapList = new ArrayList<>();
        mapList.add(Maps.newHashMap("volume", 1));
        mapList.get(0).put("brands", "西门子");
        mapList.add(Maps.newHashMap("volume", 3));
        mapList.get(1).put("brands", "西门子");
        mapList.add(Maps.newHashMap("volume", 5));
        mapList.get(2).put("brands", "IBM");



        System.out.println(SpelTest.sum(mapList, "volume"));

        String el = "T(com.inter3i.reportapi.SpelTest).sum(#mapList,'volume')";
        EvaluationContext context = new StandardEvaluationContext(  );
        context.setVariable( "mapList", mapList );
        ExpressionParser parser = new SpelExpressionParser();
        Integer value = parser.parseExpression(el).getValue(context,Integer.class);
        System.out.println(value);

        String el2 = "T(com.inter3i.reportapi.SpelTest).sum(#mapList,'volume','brands','IBM')";
        Integer value2 = parser.parseExpression(el2).getValue(context,Integer.class);
        System.out.println(value2);

    }

    public static int sum(List<Map> mapList, String key){
        return mapList.stream().mapToInt(e->(int)e.get(key)).sum();
    }
    public static int sum(List<Map> mapList, String key,String filterKey,String filterVal){
        return mapList.stream().filter(e->e.get(filterKey).equals(filterVal)).mapToInt(e->(int)e.get(key)).sum();
    }


}
