package com.akuchen.trace.api.common.utils;

import com.akuchen.trace.api.common.constant.SystemConstant;
import com.akuchen.trace.api.common.dto.MessageLogDTO;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 通讯文件类
 * 生成文件,和trace主服务进行通讯
 */
public class MessageLogUtils {

    /**
     *
     */
    private static File FILE;

    /**
     *
     * @param messageLogDTO
     * @param append true  追加  false  不追加
     * @throws IOException
     */
    public static void write(MessageLogDTO messageLogDTO,boolean append)  {
        try {
            if(append){
                String json = FileUtils.readFileToString(FILE);
                MessageLogDTO oldMessageLog = JSON.parseObject(json, MessageLogDTO.class);
                BeanUtils.copyPropertiesIgnoreNull(oldMessageLog,messageLogDTO);
            }
            FileUtils.write(FILE,JSON.toJSONString(messageLogDTO),false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 读的时候 是没有地址的,
     * @param path
     */
    public static MessageLogDTO read(String path){
        try {
            File file = new File(path);
            if (!file.exists()){
                return new MessageLogDTO();
            }
            String json = FileUtils.readFileToString(file);
            MessageLogDTO oldMessageLog = JSON.parseObject(json, MessageLogDTO.class);
            return oldMessageLog;
        }catch (Exception e){
            return new MessageLogDTO();
        }
    }

    public static void setFile(File file){
        FILE=file;
    }
}
