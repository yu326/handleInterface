package com.inter3i.reportapi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by zhuguowei on 9/18/17.
 */
@Data
public class Foo {
    private int id;
    private String name;
    private LocalDateTime createTime;
}
