package com.akuchen.trace.parse.builder;

import com.akuchen.trace.api.common.dto.MessageLogDTO;
import com.akuchen.trace.api.common.utils.MessageLogUtils;
import com.akuchen.trace.parse.dto.CodeInfoDTO;
import com.akuchen.trace.parse.dto.DebuggerDTO;
import com.akuchen.trace.parse.dto.TemplateReq;

import java.util.List;
import java.util.Map;

/**
 * 生成jdi监控文件
 * <p>
 * 本来没有必要生成这个文件, 明明可以直接触发所有步骤
 * 但是因为生成mock文件可能无法直接执行成功, 需要手动注释类多态方法 或者main执行的默认方法不是目标方法(如果这两问题可以解决 那这个类以后也就可以不要了)
 */
public class JdiClassBuilder {


	public static String template(TemplateReq templateReq, String gptFilePath) {


		String code = "package " + templateReq.getPackageName() + ";\n" +
					  "\n" +
					  "import com.akuchen.trace.parse.dto.DebuggerDTO;\n" +
					  "import com.akuchen.trace.parse.jdi.JdiMethodManager;\n" +
					  "import com.akuchen.trace.parse.jdi.rmi.RmiServer;\n" +
					  "import lombok.extern.slf4j.Slf4j;\n" +
					  "\n" +
					  "import java.util.ArrayList;\n" +
					  "import java.util.List;\n" +
					  "\n" +
					  "@Slf4j\n" +
					  "public class " + templateReq.getJdiClassName() + " {\n" +
					  "\n" +
					  "    public static void main(String[] args) {\n" +
					  "        JdiMethodManager jdiMethodManager = JdiMethodManager.getInstance();\n" +
					  "        jdiMethodManager.setTestClassName(\"" + templateReq.getPackageName() + "." +
					  templateReq.getClassName() + "\");\n" +
					  "        jdiMethodManager.setStartMainClass(" + templateReq.getJdiClassName() + ".class);\n" +
					  "        jdiMethodManager.setGptFilePath(\"" + gptFilePath + "\");\n" +
					  "        List<DebuggerDTO> debuggerDTOS = new ArrayList<>();\n" +
					  debuggerCode(templateReq) +
					  "        jdiMethodManager.setDebuggerDTOS(debuggerDTOS);\n" +
					  "\n" +
					  "        try {\n" +
					  "            //启动rmi服务端 让其他jvm发送通知用的\n" +
					  "            log.info(\"start rmi server...\");\n" +
					  "            RmiServer.run();\n" +
					  "            //启动jdi\n" +
					  "            log.info(\"start jdi...\");\n" +
					  "            jdiMethodManager.run();\n" +
					  "            log.info(\"jdi rmi stop...\");\n" +
					  "            //关闭rmi\n" +
					  "            RmiServer.stop();\n" +
					  "        } catch (Exception e) {\n" +
					  "            log.error(e.getMessage(),e);\n" +
					  "        }\n" +
					  "        log.info(\"System.exit...\");\n" +
					  "        System.exit(0);\n" +
					  "    }\n" +
					  "}";
		return code;

	}

	private static String debuggerCode(TemplateReq templateReq) {
		String code = "";
		for (CodeInfoDTO codeInfoDTO : templateReq.getCodeInfoDTOS()) {
			for (Map.Entry<String, List<DebuggerDTO>> entry : codeInfoDTO.getDebuggerMap().entrySet()) {

				String escapedLongString = entry.getKey().replace("\n", "\"+\n\"");
				for (DebuggerDTO debuggerDTO : entry.getValue()) {
					code += "        debuggerDTOS.add(DebuggerDTO.builder().className(\"" + debuggerDTO.getClassName() +
							"\").classLine(" + debuggerDTO.getClassLine() + ").stack(\"" + escapedLongString +
							"\").build());\n";
					//通知trace
					MessageLogDTO messageLogDTO = new MessageLogDTO();
					messageLogDTO.setIfExceptionStack(true);
					MessageLogUtils.write(messageLogDTO, false);
				}
			}
		}
		return code;
	}


}
