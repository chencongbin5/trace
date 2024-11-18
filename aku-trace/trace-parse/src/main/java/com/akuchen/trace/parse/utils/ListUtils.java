package com.akuchen.trace.parse.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ListUtils {

    public static List<String> sortByDate(List<String> logs) {
        List<String> sortLogs = logs.stream().sorted((t1, t2) -> {
            Date date1 = parseTime(t1);
            Date date2 = parseTime(t2);
            return date1.compareTo(date2);
        }).collect(Collectors.toList());
        return sortLogs;
    }

    private static Date parseTime(String content) {
        Date result = null;
        try {
            result = DateUtils.parseDate("2099-01-01", "yyyy-MM-dd");
            String date = parseStringDate(content);
            if (StringUtils.isEmpty(date)) {
                return result;
            }
            return DateUtils.parseDate(date, "yyyy-MM-dd HH:mm:ss.SSS");
        } catch (ParseException e) {
            log.error("date parse error: content:{}", content);
        }
        return result;
    }

    private static String parseStringDate(String content) {
        String regex = "\\|.{23}\\|";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String group = matcher.group(0);
            //去头尾|
            return group.substring(1, group.length() - 1);
        }
        return null;
    }

    public static String parseStringThread(String content) {
        try {
//            String dateAndTid = parseStringDateAndThread(content);
//            return dateAndTid.substring(dateAndTid.indexOf(parseStringDate(content)) + 23);
            //日志格式修改了 跟着改
           return  content.substring(0, content.indexOf("|") + 1);
        }catch (Exception e){
            return null;
        }
    }

    private static String parseStringDateAndThread(String content) {
        String regex = "\\|.{23}\\|.{0,100}\\|TID:";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String group = matcher.group(0);
            //去头尾|
            return group.substring(1, group.length() - 4);
        }
        return null;
    }
}
