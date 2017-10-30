package com.inter3i.reportapi;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhuguowei on 9/14/17.
 */
public class RegexTest {

    @Test
    public void testMatch(){
        assertEquals(true, "[brands]".matches("\\[(\\w+)]"));
        assertEquals(true, "[brands]".matches("\\[(\\w+)].*"));
        assertEquals(true, "[brands],platform".matches("\\[(\\w+)].*"));
        assertEquals(false, "screen_name,platform".matches("\\[(\\w+)].*"));

        assertEquals(true, "0,1".matches("[0-9]+(,*[0-9]*)*"));
        assertEquals(true, "0,1,2".matches("[0-9]+(,*[0-9]*)*"));
        assertEquals(true, "0,".matches("[0-9]+(,*[0-9]*)*"));
        assertEquals(true, "0".matches("[0-9]+(,*[0-9]*)*"));
        assertEquals(false, "".matches("[0-9]+(,*[0-9]*)*"));
        assertEquals(false, "1a".matches("[0-9]+(,*[0-9]*)*"));

        assertEquals(false, "1a".matches("[0-9]+"));
        assertEquals(false, "a1".matches("[0-9]+"));




    }
    @Test
    public void testExtract(){
        String groupBy = "[brands]";
        String unwindKey = groupBy.replaceFirst("\\[(\\w+)].*", "$1");
        assertEquals("brands",unwindKey);

        groupBy = "[brands],platform";
        unwindKey = groupBy.replaceFirst("\\[(\\w+)].*", "$1");
        assertEquals("brands",unwindKey);

        groupBy = "screen_name,platform";
        unwindKey = groupBy.replaceFirst("\\[(\\w+)].*", "$1");
        assertEquals(groupBy,unwindKey);
    }
    @Test
    public void testReplaceAll(){
        assertEquals("brands","[brands]".replaceAll("[\\[\\]]", ""));
        assertEquals("brands,platform","[brands],platform".replaceAll("[\\[\\]]", ""));
        assertEquals("screen_name,platform","screen_name,platform".replaceAll("[\\[\\]]", ""));
    }
    @Test
    public void testExtractByMatcher(){
        String el = "sum(#{dept},#{gender},'salary')";

        Pattern pattern = Pattern.compile("#\\{(\\w+)}");
        Matcher matcher = pattern.matcher(el);
        while(matcher.find()){
            System.out.println(matcher.group(1));
        }
    }
}
