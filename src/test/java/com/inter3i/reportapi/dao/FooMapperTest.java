package com.inter3i.reportapi.dao;

import com.inter3i.reportapi.entity.Foo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by zhuguowei on 9/18/17.
 */
@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class FooMapperTest {
    @Autowired
    private FooMapper fooMapper;
    @Test
    public void findById() throws Exception {
        Foo foo = fooMapper.findById(1);
        Assert.assertEquals("aaa",foo.getName());
    }

}