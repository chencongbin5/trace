package com.akuchen.trace.service.mapper;

import org.apache.ibatis.annotations.Param;

import com.akuchen.trace.service.entity.TraceOrder;

public interface TraceOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(TraceOrder record);

    TraceOrder selectByPrimaryKey(Long id);
    TraceOrder selectByTid(String tid);

    TraceOrder selectByTidAndAppName(@Param("tid") String tid,@Param("appName") String appName);

    int updateByPrimaryKeySelective(TraceOrder record);

}