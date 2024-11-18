package com.akuchen.trace.parse.utils;

import com.akuchen.trace.parse.enums.JdiTypeEnum;
import com.sun.jdi.*;
import com.sun.tools.jdi.ArrayReferenceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ValueConverter {
    public static Object convertValue(Value value,ThreadReference threadReference) {
        return convertValue(value,new HashMap<>(),0,threadReference);
    }

    private static Object convertValue(Value value, HashMap<ObjectReference,Object> processedObjects,Integer level,ThreadReference threadReference) {
        if(level>=5){
            //防止递归过深,导致内存溢出
            return null;
        }
        if (value instanceof BooleanValue) {
            return ((BooleanValue) value).value();
        } else if (value instanceof ByteValue) {
            return ((ByteValue) value).value();
        } else if (value instanceof CharValue) {
            return ((CharValue) value).value();
        } else if (value instanceof ShortValue) {
            return ((ShortValue) value).value();
        } else if (value instanceof IntegerValue) {
            return ((IntegerValue) value).value();
        } else if (value instanceof LongValue) {
            return ((LongValue) value).value();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).value();
        } else if (value instanceof DoubleValue) {
            return ((DoubleValue) value).value();
        } else if (value instanceof StringReference) {
            return ((StringReference) value).value();
        }else if(value instanceof ArrayReference){
            List<Value> list = ((ArrayReferenceImpl) value).getValues();
            List<Object> collect = list.stream().map(t->convertValue(t,processedObjects,level+1,threadReference)).filter(t->Objects.nonNull(t)).collect(Collectors.toList());
            return collect;
        }
        else if (value instanceof ObjectReference) {
            ObjectReference objectRef = (ObjectReference) value;
            if (processedObjects.containsKey(objectRef)) {
                return processedObjects.get(objectRef); // Avoid processing the same object reference again
            }
            processedObjects.put(objectRef,null);
            String result = JdiTypeEnum.fieldByName(value);
            if (Objects.nonNull(result)) {
                processedObjects.put(objectRef,result);
                return result;
            }
            Method method = objectRef.referenceType().methodsByName("toString","()Ljava/lang/String;").stream().findFirst().orElse(null);
            if (Objects.nonNull(method)) {
                try {
                    Value invokeResult = objectRef.invokeMethod(threadReference, method, Collections.emptyList(), 0);
                    if (invokeResult instanceof StringReference) {
                        String toString = ((StringReference) invokeResult).value();
                        processedObjects.put(objectRef,toString);
                        return toString;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            ReferenceType type = objectRef.referenceType();
//            if (type instanceof ClassType) {
//                ClassType classType = (ClassType) type;
//                //Map<Field, Value> fields = objectRef.getValues(classType.allFields());
//                Map<Field, Value> fields = objectRef.getValues(classType.visibleFields());
//                // 创建一个 Map 用于存储字段名和对应的值
//                Map<String, Object> fieldValues = new HashMap<>();
//                for (Map.Entry<Field, Value> entry : fields.entrySet()) {
//                    Field field = entry.getKey();
//                    Value fieldValue = entry.getValue();
//                    Object convertedValue = convertValue(fieldValue, processedObjects,level+1,threadReference);
//                    if (Objects.isNull(convertedValue)) {
//                        continue;
//                    }
//                    if(convertedValue instanceof List){
//                        List list = (List) convertedValue;
//                        if(CollectionUtils.isEmpty(list)){
//                            continue;
//                        }
//                    }
//                    if(convertedValue instanceof Map){
//                        Map map = (Map) convertedValue;
//                        if(MapUtils.isEmpty(map)){
//                            continue;
//                        }
//                    }
//                    fieldValues.put(field.name(), convertedValue);
//                }
//                processedObjects.put(objectRef,fieldValues);
//                return fieldValues;
//            }
        }

        return null;
    }
}




