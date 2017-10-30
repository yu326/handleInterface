package com.inter3i.reportapi.dao;

import com.inter3i.reportapi.entity.Foo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhuguowei on 9/18/17.
 */
@Mapper
public interface FooMapper {

    @Select("SELECT * FROM subjectAttribute WHERE id = #{id}")
    Foo findById(int id);
}
