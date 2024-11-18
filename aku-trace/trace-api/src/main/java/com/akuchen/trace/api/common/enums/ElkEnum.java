package com.akuchen.trace.api.common.enums;


public enum ElkEnum {

    AKU("aku", "com.akuchen.trace.parse.http.EsQueryHttp"),
    OWNBANK("ownbank","com.akuchen.trace.parse.http.EsQueryOwnbankHttp"),
    ;


    private String name;
    private String clazz;
    ElkEnum(String name, String clazz){
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public String getClazz() {
        return clazz;
    }

    public static ElkEnum getEnum(String name){
        for(ElkEnum e : ElkEnum.values()){
            if(e.getName().equals(name)){
                return e;
            }
        }
        return null;
    }
}
