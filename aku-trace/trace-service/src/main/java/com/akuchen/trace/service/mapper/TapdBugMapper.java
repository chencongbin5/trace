package com.akuchen.trace.service.mapper;

import org.apache.ibatis.annotations.Param;

import com.akuchen.trace.service.entity.TapdBug;

import java.util.List;

public interface TapdBugMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TapdBug record);

    int insertSelective(TapdBug record);

    TapdBug selectByPrimaryKey(Long id);
    TapdBug selectByTapdBugId(String tapdBugId);

    List<TapdBug> selectByTidAndAppName(@Param("tid")String tid, @Param("appName")String appName,@Param("tapdBugId")String tapdBugId);

    List<TapdBug> selectCreateOrderFailedBugs();

    int updateByPrimaryKeySelective(TapdBug record);

    int updateByPrimaryKey(TapdBug record);
}