package com.akuchen.trace.parse.dto;

import lombok.Data;

@Data
public class Message {
    private String role;
    private String content;
    private String finish_reason;
}
