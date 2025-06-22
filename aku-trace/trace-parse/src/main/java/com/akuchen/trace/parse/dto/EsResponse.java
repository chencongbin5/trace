package com.akuchen.trace.parse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EsResponse implements Serializable {

    private Hits hits;
    @Data
    public static class Hits implements Serializable {
        private Total total;
        private List<Detail> hits;
    }
    @Data
    public static class Total implements Serializable {
        private Integer value;
    }
    @Data
    public static class Detail implements Serializable {
        private String _index;
        private String _type;
        private String _id;
        private Source _source;
    }
    @Data
    public static class Source implements Serializable {
        private String message;
    }
}
