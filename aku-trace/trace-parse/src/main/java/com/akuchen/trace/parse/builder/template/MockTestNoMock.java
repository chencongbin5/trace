package com.akuchen.trace.parse.builder.template;

import org.apache.commons.lang3.StringUtils;

import com.akuchen.trace.parse.builder.MockClassBuilder;
import com.akuchen.trace.parse.builder.ProcessStackTrace;
import com.akuchen.trace.parse.builder.head.HeadEnum;
import com.akuchen.trace.parse.builder.head.HeadFactory;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.akuchen.trace.parse.dto.QueryLogAndCreateClassFileReq;
import com.akuchen.trace.parse.dto.TemplateReq;
import com.akuchen.trace.parse.utils.ListUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仅模拟入参
 */
public class MockTestNoMock extends MockClassBuilder {



	public static List<CodeInfoDTO> start(List<String> lines, QueryLogAndCreateClassFileReq req) {
		MockClassBuilder.req =req;
		boolean isAny = req.isWhenParamsIsAny();
		//可能存在多个线程 分开取
		List<List<String>> groupLines = groupLines(lines);
		List<CodeInfoDTO> codeInfoDTOS = groupLines.stream().map(threadLines -> {
			CodeInfoDTO codeInfoDTO = new CodeInfoDTO(ListUtils.parseStringThread(threadLines.get(0)));
			//given 信息
			codeInfoDTO.getBodyList().add("//given");
			//[请求发起入口]
			codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.RPC.getType(), codeInfoDTO.getWhenCount(), req.getOutFilePath()));
			codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.MQ.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
			codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.JOB.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
			codeInfoDTO.addCodeInfo(HeadFactory.doDisposeHead(threadLines, HeadEnum.BIZ.getType(), codeInfoDTO.getWhenCount(),req.getOutFilePath()));
			//堆栈异常信息
			Map<String, List<DebuggerDTO>> map = ProcessStackTrace.filterErrorLog(threadLines);
			codeInfoDTO.setDebuggerMap(map);

			//check
			codeInfoDTO.check();

			return codeInfoDTO;
		}).filter(codeInfo -> codeInfo.getBodyList().size()>1).collect(Collectors.toList());
		//最后检查
		finalCheck(codeInfoDTOS);
		return codeInfoDTOS;

	}




}
