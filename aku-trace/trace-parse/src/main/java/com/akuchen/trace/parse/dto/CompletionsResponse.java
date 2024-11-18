package com.akuchen.trace.parse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * charGpt   Completions 返回dto
 */
@Data
public class CompletionsResponse  implements Serializable {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;


    @Data
    public static class Choice {
        private Long index;
        private Message message;
    }

    @Data
    public static class Usage {
        private Long prompt_tokens;
        private Long completion_tokens;
        private Long total_tokens;
    }
}
