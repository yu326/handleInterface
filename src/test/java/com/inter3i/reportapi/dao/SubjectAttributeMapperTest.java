package com.inter3i.reportapi.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by koreyoshi on 2017/9/18.
 */
@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class SubjectAttributeMapperTest {
    @Autowired
    private SubjectAttributeMapper subjectAttributeMapper;
    @Test
    public void findById() throws Exception {
//        SubjectAttribute res= subjectAttributeMapper.findById(1);
//        System.out.println(res);
    }
}