package com.akuchen.trace.api.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BeanUtils {
    public static <k, T> List<T> convertList(List<k> sourceList, Class<T> zlass) {
        List<T> queryRspDTOS = sourceList.stream().map(source -> {
            T tmp = null;
            try {
                tmp = zlass.newInstance();
            } catch (Exception e) {
                log.error("newInstance error ", e);
            }
            BeanUtils.copyPropertiesIgnoreNull(source, tmp);
            return tmp;
        }).collect(Collectors.toList());
        return queryRspDTOS;
    }

    public static <k, T> T convert(k source, Class<T> zlass) {
        if(Objects.isNull(source)){
            return null;
        }
        T tmp = null;
        try {
            tmp = zlass.newInstance();
        } catch (Exception e) {
            log.error("newInstance error ", e);
        }
        BeanUtils.copyPropertiesIgnoreNull(source, tmp);
        return tmp;
    }

    /**
     * @param source 要拷贝的对象
     * @return
     * @Description <p>获取到对象中属性为null的属性名  </P>
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * @param source 源对象
     * @param target 目标对象
     * @Description <p> 拷贝非空对象属性值 </P>
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }
}
