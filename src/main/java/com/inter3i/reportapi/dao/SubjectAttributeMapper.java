package com.inter3i.reportapi.dao;

import com.inter3i.reportapi.entity.SubjectAttribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by koreyoshi on 2017/9/18.
 */
@Mapper
public interface SubjectAttributeMapper {

    @Select("SELECT * FROM subjectAttribute WHERE subjectid = #{sid} and charttype = #{charttype}")
    SubjectAttribute findById( @Param("sid")int sid,@Param("charttype") int charttype);

}
