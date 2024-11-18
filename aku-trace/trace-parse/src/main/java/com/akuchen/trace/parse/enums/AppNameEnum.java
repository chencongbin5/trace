//package com.akuchen.trace.parse.enums;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
///**
// * 测试环境log  的appName的准确名称
// */
//@Getter
//@RequiredArgsConstructor
//public enum AppNameEnum {
//
//
//    AKU_EC_DATA_REPORT( "aku-ec-data-report","aku-ec-data-report-service","https://git.silvrr.com/aku-ec-report/aku-ec-data-report.git"),
//    OWNBANK_FULFILLMENT_JOB("ownbank-fulfillment-job",null,null),
//    OWNBANK_FULFILLMENT_SERVICE("ownbank-fulfillment-service",null,null),
//    ORDER_VIOLATION_SERVICE("order-violation-service","order-violation-impl","https://git.silvrr.com/aku-ec-service/order-violation-service.git"),
//    ORDER_BACKGROUND_SERVICE("order-background-service","order-background-service","https://git.silvrr.com/aku-ec-service/order-service.git"),
//    VENDOR_ORDERS_BIZ("vendor-orders-biz",null,null),
//    ORDER_SERVICE_JOB("order-service-job","order-service-job","https://git.silvrr.com/aku-ec-service/order-service.git"),
//    DATA_SYNC_SERVICE("data-sync-service","","https://git.silvrr.com/aku-ec-service/data-sync-service.git"),
//    CALCULATION_SERVICE("calculation-service","calculation-service-impl","https://git.silvrr.com/aku-ec-service/calculation-service.git"),
//    AKU_PUSH_SERVICE("aku-push-service","aku-push-service-server","https://git.silvrr.com/aku-ec-service/aku-push-service.git"),
//    TRADE_BIZ("trade-biz","trade-biz-app","https://git.silvrr.com/aku-ec-service/trade-biz.git"),
//    CONSOLE_BIZ_ORDER("console-biz-order","","https://git.silvrr.com/aku-ec-biz/console-biz-order.git"),
//    FULFILLMENT_SERVICE("fulfillment-service","fulfillment-service","https://git.silvrr.com/aku-ec-service/fulfillment-parent.git"),
//    ORDER_SETTLEMENT_SERVICE("order-settlement-service","order-settlement-server","https://git.silvrr.com/aku-ec-service/order-settlement-service.git"),
//    ORDER_SERVICE("order-service","order-service-impl","https://git.silvrr.com/aku-ec-service/order-service.git"),
//
//    ;
//    /**
//     * 白象里面的名称, 项目实际目录可能不是这个,  但是分布式日志是这个
//     */
//    private final String appName;
//    /**
//     * 实际git上的最小目录下的项目名称
//     * 如果为空  说明没有子目录 这就是最小目录
//     */
//    private final String serviceName;
//    private final String gitlabUrl;
//
//    public static AppNameEnum of(String appName){
//        for (AppNameEnum value : AppNameEnum.values()) {
//            if (value.appName.equals(appName)) {
//                return value;
//            }
//        }
//        return null; // or throw an exception
//    }
//
//}
