package com.akuchen.trace.service.controller;

import com.akuchen.trace.api.common.dto.ApiResult;
import com.akuchen.trace.api.common.dto.TraceLogMsgDTO;
import com.akuchen.trace.parse.utils.ListUtils;
import com.akuchen.trace.service.entity.dto.req.AddReportReqDTO;
import com.akuchen.trace.service.service.TraceLogService;
import com.akuchen.trace.service.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping({"api/report"})
public class ReportController {


    @Autowired
    protected TraceLogService traceLogService;

    /**
     * 新增上报记录
     */
    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ApiResult<String> addReport(@RequestBody AddReportReqDTO addReportReqDTO) {
        if (addReportReqDTO == null || addReportReqDTO.getLog() == null) {
            return ApiResult.errorResponse("102","上报的日志不能为空");
        }
        //注意重复上报问题
        //异步写入
        ThreadPoolUtils.submitTask(() -> {
			List<String> lines = convertToList(addReportReqDTO.getLog());
			lines = lines.stream().filter(t -> Objects.nonNull(t) && !t.isEmpty()).collect(Collectors.toList());
			Map<String, List<String>> linesGroupByThread = lines.stream().collect(Collectors.groupingBy(t -> ListUtils.parseStringThread(t)));
			linesGroupByThread.forEach((thread, logList) -> {
				for (String log : logList) {
					TraceLogMsgDTO traceLogMsgDTO = new TraceLogMsgDTO();
					traceLogMsgDTO.setTid(addReportReqDTO.getTid());
					traceLogMsgDTO.setServiceName(addReportReqDTO.getServiceName());
					traceLogMsgDTO.setThreadName(thread);
                    //这部分数据 本身就是有序的,时间字段就不重要了
					traceLogMsgDTO.setLogTime(new Date());
					traceLogMsgDTO.setLog(log);
					traceLogService.insert(traceLogMsgDTO);
				}
			});
		});

        return ApiResult.success("上报成功");


    }

	public static List<String> convertToList(String logString) {
		String[] lines = logString.split("\r\n");
		List<String> logLines = new ArrayList<>();
		for (String line : lines) {
			if (line.startsWith("\tat")) {
				int lastIndex = logLines.size() - 1;
				logLines.set(lastIndex, logLines.get(lastIndex) + "\n" + line);
			} else {
				logLines.add(line);
			}
		}
		return logLines;
	}






}
