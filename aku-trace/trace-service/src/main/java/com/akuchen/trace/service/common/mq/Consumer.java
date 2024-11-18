//package com.akuchen.trace.service.common.mq;
//
//import com.akuchen.platform.components.mq.annotations.AlMQConsumer;
//import com.akuchen.platform.components.mq.common.consumer.MQMessageResponse;
//import com.akuchen.platform.components.mq.constants.MQMessageStatus;
//import com.akuchen.platform.components.mq.constants.MQType;
//import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
//import com.akuchen.trace.service.mapper.TraceLogMapper;
//import com.akuchen.trace.service.service.TraceLogService;
//import com.akuchen.trace.service.util.RedisUtils;
//import com.alibaba.fastjson.JSONObject;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Objects;
//
///** 此类需要重构,  用原生的mq方法
// * mq消费入口
// */
//@Component
//@Slf4j
//public class Consumer {
//
//
//    @Autowired
//    private RedisUtils redisUtils;
//
//    @Autowired
//    protected TraceLogService traceLogService;
//    @Autowired
//    private TraceLogMapper traceLogMapper;
//
//    /**
//     * 退款成功 创建未完成订单违规记录
//     *
//     * @param message
//     * @return
//     */
//    @AlMQConsumer(topic = "trace_topic", subscribeExpression = "log", consumerGroup = "trace_log_topic_group", order = true, mqType = MQType.ROCKETMQ)
//    public MQMessageStatus addTraceLog(MQMessageResponse<TraceLogMsgDTO> message) {
//        try {
//            log.info("addTraceLog, msg: {}", JSONObject.toJSON(message));
//            TraceLogMsgDTO traceLogMsgDTO = message.getMsgs();
//            MQMessageStatus mqMessageStatus = (MQMessageStatus) redisUtils.lockAndProcess("log:" + traceLogMsgDTO.getTid(), 6L, object -> {
//
//                Integer result = traceLogService.insert(traceLogMsgDTO);
//                if (result > 0) {
//                    return MQMessageStatus.SUCCESS;
//                }
//                log.info("addTraceLog error, TraceLogMsgDTO: {}", JSONObject.toJSON(traceLogMsgDTO));
//                return MQMessageStatus.FAILED;
//            });
//            if (Objects.isNull(mqMessageStatus)) {
//                return MQMessageStatus.FAILED;
//            }
//            return mqMessageStatus;
//        } catch (Throwable e) {
//            log.error("consumer failed", e);
//            return MQMessageStatus.FAILED;
//        }
//    }
//
//
//}
//
