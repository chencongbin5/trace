//package com.akuchen.trace.report.mq;
//
//import com.akuchen.platform.components.mq.annotations.AlMQProducer;
//import com.akuchen.platform.components.mq.common.producer.AlMessageProducer;
//import com.akuchen.platform.components.mq.common.producer.MessageSendResult;
//import com.akuchen.platform.components.mq.common.producer.RocketMQMessage;
//import com.akuchen.platform.components.mq.common.producer.SendResultCallback;
//import com.akuchen.platform.components.mq.constants.MQMessageStatus;
//import com.akuchen.platform.components.mq.constants.MQType;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Slf4j
//public class TraceMsgProducer {
//
//    private static ThreadLocal<List<Long>> waitSendMqMsgListThreadLocal = new ThreadLocal<>();
//
//    @AlMQProducer(mqType = MQType.ROCKETMQ)
//    private AlMessageProducer producer;
//
//
//    /**
//     * 同步顺序发送rocketmq 消息
//     * @param topic 消息topic
//     *            @see
//     * @param tag 标识消息的类型，方便消费者进行消息过滤
//     *            @see
//     * @param hashKey 发送路由到同一个队列的hashKey
//     * @param content 消息内容
//     **/
//    public void syncSendOrderly(String topic, String tag, String hashKey, String content){
//        try{
//            RocketMQMessage rocketMQMessage = new RocketMQMessage(topic, content,hashKey, tag);
//            MessageSendResult messageSendResult = producer.sendMessage(rocketMQMessage);
//            if(MQMessageStatus.SUCCESS == messageSendResult.getMessageStatus()){
//                //log.info("syncSendOrderly success. messageId={}, topic={}, tag={}, hashKey={}, message={}", messageSendResult.getMessageId(), topic, tag, hashKey, content);
//            } else {
//                log.error("syncSendOrderly fail. messageId={}, topic={}, tag={}, hashKey={}, message={}, error:", messageSendResult.getMessageId(), topic, tag, hashKey, content, messageSendResult.getE());
//                throw new RuntimeException("send error");
//            }
//        } catch (Exception e){
//            log.error("syncSendOrderly fail,topic={}, tag={}, hashKey={}, message={}, error:", topic, tag, hashKey, content, e);
//            throw new RuntimeException("send error");
//        }
//    }
//
//
//
//    /**
//     * 异步顺序发送rocketmq 消息
//     * @param topic 消息topic
//     *            @see
//     * @param tag 标识消息的类型，方便消费者进行消息过滤
//     *            @see
//     * @param hashKey 发送路由到同一个队列的hashKey
//     * @param content 消息内容
//     **/
//    public void asyncSendOrderly(String topic, String tag, String hashKey, String content, SendResultCallback sendCallback){
//        try{
//            RocketMQMessage rocketMQMessage = new RocketMQMessage(topic, content,hashKey, tag);
//            rocketMQMessage.setOrder(true);
//            producer.sendMessageAsync(rocketMQMessage, sendCallback);
//        }catch (Exception e){
//            log.error("asyncSendOrderly fail,topic={}, tag={}, hashKey={}, message={}, error:", topic, tag, hashKey, content, e);
//
//        }
//    }
//
//
//
//
//}
//
