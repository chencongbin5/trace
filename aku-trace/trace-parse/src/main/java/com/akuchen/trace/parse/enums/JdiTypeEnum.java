package com.akuchen.trace.parse.enums;

import com.sun.jdi.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 基本类型转
 */
@Getter
@RequiredArgsConstructor
public enum JdiTypeEnum {
//byte、char、short、int、long、float、double、boolean

    BYTE( "java.lang.Byte", "value"),
    CHAR( "java.lang.Character","value"),
    SHORT( "java.lang.Short","value"),
    INT( "java.lang.Integer","value"),
    LONG( "java.lang.Long","value"),
    FLOAT( "java.lang.Float","value"),
    DOUBLE( "java.lang.Double","value"),
    BOOLEAN( "java.lang.Boolean","value"),
    //BigDecimal( "java.math.BigDecimal","intValue"),

    ;

    private String value;
    private String fieldName;

    JdiTypeEnum( String value,String fieldName){
        this.value=value;
        this.fieldName=fieldName;
    }

    private static String findFieldName(String value){
        JdiTypeEnum typeEnum = Arrays.stream(JdiTypeEnum.values()).filter(t -> Objects.equals(value, t.getValue())).findFirst().orElse(null);
        return Optional.ofNullable(typeEnum).map(JdiTypeEnum::getFieldName).orElse(null);
    }

    public static String fieldByName(Value value){
        ObjectReference objectRef = (ObjectReference) value;
        String fieldName = findFieldName(value.type().name());
        String result = Optional.ofNullable(fieldName).map(val -> objectRef.getValue(objectRef.referenceType().fieldByName(fieldName)).toString()).orElse(null);
        return result;
    }


}
