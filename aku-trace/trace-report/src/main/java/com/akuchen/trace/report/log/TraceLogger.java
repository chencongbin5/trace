package com.akuchen.trace.report.log;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.report.common.utils.DateUtil;
import com.akuchen.trace.report.common.utils.TraceIdUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class TraceLogger {
    private static Logger logger = LoggerFactory.getLogger("TraceLogger");

    /**
     * append log
     *
     * @param callInfo
     * @param appendLog
     */
    private static Boolean logDetail(String application, StackTraceElement callInfo, String appendLog) {

        Boolean isCreateTid=false;
        /*// "yyyy-MM-dd HH:mm:ss [ClassName]-[MethodName]-[LineNumber]-[ThreadName] log";
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        StackTraceElement callInfo = stackTraceElements[1];*/

        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append(DateUtil.formatDateTime(new Date())).append(" ")
//                    .append("["+ callInfo.getClassName() + "#" + callInfo.getMethodName() +"]").append("-")
//                    .append("["+ callInfo.getLineNumber() +"]").append("-")
//                    .append("["+ Thread.currentThread().getName() +"]").append(" ")
//                    .append(appendLog!=null?appendLog:"");

        stringBuffer.append(Thread.currentThread().getName()).append("|")
                    .append(appendLog!=null?appendLog:"");

        String formatAppendLog = stringBuffer.toString();

        // appendlog
        String traceId= TraceFileAppender.contextHolder.get();
        if (traceId ==null) {
             traceId = TraceIdUtil.getTraceId();
            if (StringUtils.isEmpty(traceId) || Objects.equals(traceId.toUpperCase(),"N/A")) {
                traceId = TraceIdUtil.generateRandomString(15);
            }
            if(traceId.length()==15){
                isCreateTid=true;
            }
            //日志自定义字段
            MDC.put(SystemConstant.TRACE_TID, traceId);
            //ThreadContext.put(SystemConstant.TRACE_TID, traceId);
            TraceFileAppender.contextHolder.set(traceId);
        }
        //没有tid 打印文本  tid通过%X{traceTid}获取   改成不展示, 因为有的打印打算从log那里继承,, 不展示也不影响啥
//        if (StringUtils.isEmpty(TraceIdUtil.getTraceId()) || Objects.equals(TraceIdUtil.getTraceId().toUpperCase(),"N/A")) {
//            logger.info(formatAppendLog);
//        }
        String logFileName = TraceFileAppender.makeLogFileName(new Date(), traceId + "#"+ application);
        TraceFileAppender.appendLog(logFileName, formatAppendLog);
        return isCreateTid;
    }

    /**
     * append log with pattern
     *
     * @param appendLogPattern  like "aaa {} bbb {} ccc"
     * @param appendLogArguments    like "111, true"
     */
    public static Boolean log(String application , String appendLogPattern,Object ... appendLogArguments) {

    	FormattingTuple ft = MessageFormatter.arrayFormat(appendLogPattern, appendLogArguments);
        String appendLog = ft.getMessage();

        /*appendLog = appendLogPattern;
        if (appendLogArguments!=null && appendLogArguments.length>0) {
            appendLog = MessageFormat.format(appendLogPattern, appendLogArguments);
        }*/

        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        return logDetail(application, callInfo, appendLog);
    }

    /**
     * append exception stack
     *
     * @param e
     */
    public static void log(String application,Throwable e) {

        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String appendLog = stringWriter.toString();

        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        logDetail(application, callInfo, appendLog);
    }

}
