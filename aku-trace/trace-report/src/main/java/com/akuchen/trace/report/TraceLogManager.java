package com.akuchen.trace.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.akuchen.trace.api.common.enums.SendTypeEnum;
import com.akuchen.trace.report.log.TraceLogger;

import java.util.Objects;

@Slf4j
@Component
public class TraceLogManager {


	@Value("${trace.enable:false}")
	private Boolean traceEnable;

	/**
	 * 默认log
	 */
	@Value("${trace.report:log}")
	private String report;



	public static String applicationName;

	@Value("${spring.application.name}")
	public void setApplicationName(String name) {
		TraceLogManager.applicationName = name;
	}
	/**
	 * 是否生成tid
	 * @param var1
	 * @param var2
	 * @return
	 */
    public Boolean info(String var1, Object... var2){
		if(!traceEnable){
			//项目没启用, 直接返回
			return false;
		}

        if(Objects.equals(report, SendTypeEnum.LOG.getDesc())){
            //写日志
            log.info(var1, var2);
//        } else if(Objects.equals(report, SendTypeEnum.MQ.getDesc())){
            //修改占位符
//            String result=var1.replaceAll("\\{\\}", "%s");
//            String traceLog = String.format(result, var2);
//            //写MQ
//            TraceLogMsgDTO traceLogMsgDTO = new TraceLogMsgDTO();
//            traceLogMsgDTO.setTid(TraceIdUtil.getTraceId());
//            traceLogMsgDTO.setThreadName(Thread.currentThread().getName());
//            traceLogMsgDTO.setServiceName(applicationName);
//            traceLogMsgDTO.setLogTime(new Date());
//            traceLogMsgDTO.setLog(traceLog);
//			String json = JSON.toJSONString(traceLogMsgDTO);
//			if (json.length()>=4194304){
//				//4194304 rocketmq content 最大值   还是改成日志的形式
//				log.info(var1, var2);
//			}
//
//			traceMsgProducer.asyncSendOrderly("trace_topic", "log", TraceIdUtil.getTraceId(), json,
//											  new SendResultCallback() {
//												  @Override
//												  public void onComplete(MessageSendResult sendResult) {
//													 log.info("send trace log status:{}.",sendResult.getMessageStatus());
//												  }
//											  });
        }else {
			//http 先落本地日志,然后再http请求上报
			return TraceLogger.log(applicationName,var1, var2);
		}
		return false;
    }
}
