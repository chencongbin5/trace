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
public enum TypeEnum {
//byte、char、short、int、long、float、double、boolean

    BYTE("byte", "java.lang.Byte", ByteValue.class, "(byte)0", "anyByte()"),
    CHAR( "char","java.lang.Character", CharValue.class,"(char)0","anyChar()"),
    SHORT( "short","java.lang.Short", ShortValue.class,"(short)0","anyShort()"),
    INT( "int","java.lang.Integer",IntegerValue.class,"0","anyInt()"),
    LONG( "long","java.lang.Long",LongValue.class,"(long)0","anyLong()"),
    FLOAT( "float","java.lang.Float",FloatValue.class,"(float)0","anyFloat()"),
    DOUBLE( "double","java.lang.Double",DoubleValue.class,"(double)0","anyDouble()"),
    BOOLEAN( "boolean","java.lang.Boolean", BooleanValue.class,"false","anyBoolean()"),

    ;

    private String type;
    private String value;
    private Class primitiveValue;
    private String defaultValue;
    private String anyValue;

    TypeEnum(String type,String value,Class primitiveValue,String defaultValue,String anyValue){
        this.type=type;
        this.value=value;
        this.primitiveValue=primitiveValue;
        this.defaultValue=defaultValue;
        this.anyValue=anyValue;
    }

    public static String convertType(String type){
        TypeEnum typeEnum = convertEnumType(type);
        return Optional.ofNullable(typeEnum).map(TypeEnum::getValue).orElse(type);
    }


    public static TypeEnum convertEnumType(String type){
        TypeEnum typeEnum = Arrays.stream(TypeEnum.values()).filter(t -> Objects.equals(type, t.getType())).findFirst().orElse(null);
        return typeEnum;
    }

    public static Class convertPrimitiveValue(String value){
        TypeEnum typeEnum = Arrays.stream(TypeEnum.values()).filter(t -> Objects.equals(value, t.getValue())).findFirst().orElse(null);
        return Optional.ofNullable(typeEnum).map(TypeEnum::getPrimitiveValue).orElse(null);
    }

    /**
     * 1 string 返回空字符串
     * 2 基础类型返回defaultValue字符串
     * 3 其他的统统返回null
     * 后续如果有因为返回值是null 导致编译报错有bug的情况,特事特办
     * @param type
     * @return
     */
    public static String convertDefaultValue(String type){
        if(type.equals("java.lang.String")){
            return "\"\"";
        }
        TypeEnum typeEnum = Arrays.stream(TypeEnum.values()).filter(t -> Objects.equals(type, t.getType()) ||Objects.equals(type, t.getValue())).findFirst().orElse(null);
        return Optional.ofNullable(typeEnum).map(TypeEnum::getDefaultValue).orElse(null);
    }

}
