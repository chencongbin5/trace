package com.akuchen.trace.parse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class TemplateReq {

    //private CodeInfo code;
    private List<CodeInfoDTO> codeInfoDTOS;
    private List<CodeInfoDTO> codeInfoSleepDTOS;
    private List<CodeInfoDTO> codeInfoNoMockDTOS;
    private String packageName;
    private String env;
    private String mainClass;
    private String className;
    private String jdiClassName;
    private String gptClassName;

}
