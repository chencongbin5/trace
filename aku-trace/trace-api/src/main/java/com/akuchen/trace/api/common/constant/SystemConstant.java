package com.akuchen.trace.api.common.constant;

/**
 * 系统常量
 *
 */
public class SystemConstant {

    public static final String HTTP_URL="http://192.168.1.201:32111/";
    public static final String MOCK_INIT_NAME = "Start";
    public static final String MOCK_CLASS_NAME = "MockTest";
    public static final String JDI_CLASS_NAME = "MockJdi";
    public static final String GPT_CLASS_NAME = "MockGpt";
    public static final String GPT_LOG_NAME = "gpt.log";

    public static final  String MESSAGE_LOG_NAME="message.log";

    public static final String MOCK_PACKAGE = "mock";
    public static final String MOCK_FOLDER = "/src/test/java/"+MOCK_PACKAGE;

    /**
     * %X{traceTid}
     */
    public static final String TRACE_TID="traceTid";



}
