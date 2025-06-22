package com.akuchen.trace.service.service;


import com.akuchen.trace.api.common.dto.TapdBugDTO;
import com.akuchen.trace.api.common.enums.TapdBugStatusEnum;
import com.akuchen.trace.api.common.utils.BeanUtils;
import com.akuchen.trace.service.entity.TapdBug;
import com.akuchen.trace.service.entity.dto.rsp.TapdCommentRsp;
import com.akuchen.trace.service.mapper.TapdBugMapper;
import com.akuchen.trace.service.util.TapdHttpClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class TapdBugService {

    @Autowired
    private TapdBugMapper tapdBugMapper;

    @Autowired
    private TapdHttpClient tapdHttpClient;

    public Long insertBugSelective(TapdBugDTO record) {
        TapdBug tapdBug = BeanUtils.convert(record, TapdBug.class);
        tapdBugMapper.insertSelective(tapdBug);
        return tapdBug.getId();
    }

    public TapdBug query(Long id) {
        return tapdBugMapper.selectByPrimaryKey(id);
    }

    public TapdBugDTO queryByTapdBugId(String tapdBugId) {
        TapdBug tapdBug = tapdBugMapper.selectByTapdBugId(tapdBugId);
        return BeanUtils.convert(tapdBug, TapdBugDTO.class);
    }

    public List<TapdBugDTO> queryByTidAndAppName(String tid, String appName,String tapdBugId) {
        List<TapdBug> tapdBugs = tapdBugMapper.selectByTidAndAppName(tid, appName,tapdBugId);
        return BeanUtils.convertList(tapdBugs, TapdBugDTO.class);
    }

    public List<TapdBugDTO> queryCreateOrderFailedBugs() {
        List<TapdBug> tapdBugs = tapdBugMapper.selectCreateOrderFailedBugs();
        return BeanUtils.convertList(tapdBugs, TapdBugDTO.class);
    }


    public Integer updateStatus(Long id, Integer status) {
        Optional.ofNullable(id).orElseThrow(() -> new IllegalArgumentException("id is null"));
        TapdBugDTO tapdBugDTO = new TapdBugDTO();
        tapdBugDTO.setId(id);
        tapdBugDTO.setStatus(status);
        return updateBugSelective(tapdBugDTO);
    }
    public int updateBugSelective(TapdBugDTO record) {
        TapdBug tapdBug = BeanUtils.convert(record, TapdBug.class);
        return tapdBugMapper.updateByPrimaryKeySelective(tapdBug);
    }

    public int updateBug(TapdBug record) {
        return tapdBugMapper.updateByPrimaryKey(record);
    }

    public Boolean commitBug(TapdBugDTO tapdBugDTO,String answer,Integer status) {
        // 提交回复
        //换行符切换\n-><br/>
        log.info("before:"+answer);
        answer = answer.replace("\n", "<br/>");
        //	at前边也加<br/>
        answer = answer.replace("\tat", "<br/>\tat");
        log.info("after:"+answer);
        TapdCommentRsp comment = tapdHttpClient.comment(answer, tapdBugDTO.getTapdBugId());
        if (Objects.isNull(comment)){
            //回复失败
            return false;
        }
        Integer i = updateStatus(tapdBugDTO.getId(), status);
        return i>0?true:false;
    }
}