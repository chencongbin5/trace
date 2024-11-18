package com.akuchen.trace.service.mapper;

import com.akuchen.trace.service.entity.TraceLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TraceLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TraceLog record);

    int insertSelective(TraceLog record);

    TraceLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TraceLog record);

    int updateByPrimaryKeyWithBLOBs(TraceLog record);

    int updateByPrimaryKey(TraceLog record);

    List<TraceLog> selectByTid(@Param("tid") String tid, @Param("serviceName") String serviceName);

    @Delete("delete from t_trace_log where create_time < DATE_ADD(NOW(),INTERVAL -#{beforeDay} DAY) limit 100000;")
    Integer deleteDataByCreateTime(@Param("beforeDay") Integer beforeDay);
}