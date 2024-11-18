package com.akuchen.trace.parse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompletionsRequest implements Serializable {

    private String user="ccb";
    private String model="gpt-3.5-turbo";
    private List<Message> messages;

    public  CompletionsRequest(){
        Message message=new Message();
        message.setRole("system");
        message.setContent("你是一名高级java工程师,你可以检查出异常发生原因");
        messages=new ArrayList<>();
        messages.add(message);
    }

    /**
     * 添加描述信息
     * @param content
     */
    public void addUserMessage(String content){
        Message message=new Message();
        message.setRole("user");
        message.setContent(content);
        messages.add(message);
    }


}
