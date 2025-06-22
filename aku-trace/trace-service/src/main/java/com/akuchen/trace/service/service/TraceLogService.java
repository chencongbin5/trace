package com.akuchen.trace.service.service;

import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
import com.akuchen.trace.api.common.utils.BeanUtils;
import com.akuchen.trace.service.entity.TraceLog;
import com.akuchen.trace.service.mapper.TraceLogMapper;
import com.akuchen.trace.service.util.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceLogService {

    @Autowired
    private TraceLogMapper traceLogMapper;

    public Integer insert(TraceLogMsgDTO traceLogMsgDTO) {
        TraceLog traceLog = ConvertUtils.toTraceLog(traceLogMsgDTO);
        return traceLogMapper.insertSelective(traceLog);
    }

    public List<TraceLogMsgDTO> query(String tid,String serviceName){
        List<TraceLog> traceLogs = traceLogMapper.selectByTid(tid, serviceName);
        return BeanUtils.convertList(traceLogs, TraceLogMsgDTO.class);
    }

    public Integer deleteDataByCreateTime(){
        Integer beforeDay = 3;
        return traceLogMapper.deleteDataByCreateTime(beforeDay);
    }
}
