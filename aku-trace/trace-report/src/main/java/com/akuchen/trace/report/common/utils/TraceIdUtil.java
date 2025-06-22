package com.akuchen.trace.report.common.utils;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;

import java.util.Random;

/**
 * 获取traceId
 * @author: zhangyue
 */
public class TraceIdUtil {

    private static final String TRACE_ID = "tid";
    private static final String TRACE_ID_EMPTY = "N/A";

    /**
     * 把traceId放到MDC
     *
     * @param traceId
     */
    public static void putTraceIdToMDC(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 从MDC获取traceId
     *
     * @return
     */
    public static String getTraceIdFromMDC() {
        String traceId = MDC.get(TRACE_ID);
       return TRACE_ID_EMPTY.equals(traceId) ? null : traceId;
    }

    /**
     * 获取traceId
     * @return
     */
    public static String getTraceId() {
        try {
            return TraceContext.traceId();
        }catch (Throwable e){
            return generateRandomString(15);
        }
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        return random.ints(48, 91) // 91 is exclusive, so the max is 90
                     .filter(i -> (i <= 57 || i >= 65))
                     .limit(length)
                     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                     .toString();
    }
}
