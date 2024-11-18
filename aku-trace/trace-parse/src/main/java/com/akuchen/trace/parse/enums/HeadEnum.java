package com.akuchen.trace.parse.enums;

//import com.akuchen.biz.common.enums.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum HeadEnum {


    TOKEN("token", "Cookie"){
        @Override
        public String convert(String val) {
            return val;
        }
    },
    ORIGIN("origin", "Origin") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    USERAGENT("userAgent", "User-Agent") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    USERID("userId", "X-user-id") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    DEVICEID("deviceId", "device-id") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    DEVICEUUID("deviceUuid", "deviceUUID") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
//    DEVICETYPE("deviceType", "device-type") {
//        @Override
//        public String convert(String val) {
//            return EnumDeviceType.valueOf(val).getMessage();
//        }
//    },
    APPVERSION("appVersion", "app-version") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    REACTNATIVEVERSION("reactNativeVersion", "rn-version") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    REACTCODEPUSHNATIVEVERSION("reactCodePushNativeVersion", "rn-cp-version") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
//    ENUMLANGUAGE("enumLanguage", "language-id") {
//        @Override
//        public String convert(String val) {
//            return EnumLanguage.valueOf(val).getCode().toString();
//        }
//    },
//    DEVICECOUNTRY("deviceCountry", "country-id") {
//        @Override
//        public String convert(String val) {
//            return EnumCountry.valueOf(val).getCode().toString();
//        }
//    },
//    USERCOUNTRY("userCountry", "X-user-country-id") {
//        @Override
//        public String convert(String val) {
//            return EnumCountry.valueOf(val).getCode().toString();
//        }
//    },
    CLIENTIP("clientIp", "X-Forwarded-For") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
//    ENUMCLIENTTYPE("enumClientType", "channel-way") {
//        @Override
//        public String convert(String val) {
//            return EnumClientType.valueOf(val).getCode();
//        }
//    },
//    LOGINCLIENTTYPE("loginClientType", "X-channel-way") {
//        @Override
//        public String convert(String val) {
//            return EnumClientType.valueOf(val).getCode();
//        }
//    },
//    CHANNEL("channel", "channel-id") {
//        @Override
//        public String convert(String val) {
//            return EnumChannel.valueOf(val).getCode().toString();
//        }
//    },
    ACCEPT("accept", "Accept") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    ACCEPTENCODING("acceptEncoding", "Accept-Encoding") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    ACCEPTLANGUAGE("acceptLanguage", "Accept-Language") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    REFERER("referer", "Referer") {
        @Override
        public String convert(String val) {
            return val;
        }
    },
    reportInfoMap("reportInfoMap", "reportInfo") {
        @Override
        public String convert(String val) {
            return val;
        }
    },


    ;

    private  String userRequestHeader;
    private  String HttpCommonHeader;
    public abstract String convert(String val);

    HeadEnum(String userRequestHeader, String HttpCommonHeader) {
        this.userRequestHeader=userRequestHeader;
        this.HttpCommonHeader=HttpCommonHeader;
    }

    public static HeadEnum of(String userRequestHeader){
        return Arrays.stream(HeadEnum.values()).filter(t -> Objects.equals(userRequestHeader, t.getUserRequestHeader())).findFirst().orElse(null);
    }

}
