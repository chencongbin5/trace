package com.akuchen.trace.parse.builder.template;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.akuchen.trace.parse.builder.MockClassBuilder;
import com.akuchen.trace.parse.dto.TemplateReq;

/**
 * 有入参, 且mock所有对外信息的调用  且主线程挂起, 适用于关注场景在多线程中,主线程挂起防止他退出
 */
public class MockTestSleep extends MockClassBuilder {


	public static String template(TemplateReq templateReq ,String suffix) {
		templateReq.getCodeInfoDTOS().stream().forEach(t->{
			if (CollectionUtils.isEmpty(t.getMethodList())){
				return;
			}
			int index = t.getBodyList().size()-1;
			t.getBodyList().add(index,"Thread.sleep(1000000000); //主线程挂起, 防止主线程退出");
		});


		String code = "package " + templateReq.getPackageName() + ";\n" +
					  "\n" +
					  "import org.apache.commons.io.FileUtils;\n" +
					  "import java.io.File;\n" +
					  "import com.akuchen.trace.parse.common.GlobalVariables;\n" +
					  "import org.junit.runner.JUnitCore;\n" +
					  "import org.junit.runner.Request;\n" +
					  "import org.junit.runner.Result;\n" +
					  "import org.junit.runner.notification.Failure;\n" +
					  "import com.alibaba.fastjson.TypeReference;\n" +
					  "import java.math.BigDecimal;\n" +
					  "import java.math.RoundingMode;\n" +
					  "import lombok.extern.slf4j.Slf4j;\n" +
					  "import org.mockito.internal.matchers.Equality;\n" +
					  "import org.junit.Test;\n" +
					  "import java.util.Collections;\n" +
					  "import java.util.ArrayList;\n" +
					  "import org.mockito.internal.invocation.InvocationMatcher;\n" +
					  "import org.junit.Assert;\n" +
					  "import java.io.IOException;\n" +
					  "import com.alibaba.fastjson.JSON;\n" +
					  "import org.junit.runner.RunWith;\n" +
					  "import org.springframework.beans.factory.annotation.Autowired;\n" +
					  "import org.springframework.boot.test.context.SpringBootTest;\n" +
					  "import org.springframework.boot.test.mock.mockito.MockBean;\n" +
					  "import org.springframework.test.context.ActiveProfiles;\n" +
					  "import org.springframework.test.context.junit4.SpringRunner;\n" +
					  "import org.springframework.test.web.servlet.MockMvc;\n" +
					  "import static org.mockito.Mockito.*;\n" +
					  StringUtils.join(writeImport(templateReq.getCodeInfoDTOS()), "\n") +
					  "\n" +
					  "@ActiveProfiles(profiles = \"" + templateReq.getEnv() + "\")\n" +
					  "@RunWith(SpringRunner.class)\n" +
					  "@SpringBootTest( classes = {" + templateReq.getMainClass() + ".class})\n" +
					  "@Slf4j\n" +
					  StringUtils.join(writeAnotation(templateReq.getCodeInfoDTOS()), "\n") +
					  "public class " + templateReq.getClassName()+suffix + "  {\n" +
					  "\n" +
					  //引用对象
					  "    " + StringUtils.join(writeHead(templateReq.getCodeInfoDTOS()), "\n    ") +
					  "\n" +
					  "\n" +
					  //main方法
					  writeMain(templateReq) +
					  "\n" +
					  "\n" +
					  //mock方法
					  StringUtils.join(writeBody(templateReq.getCodeInfoDTOS()), "\n") +
					  "}\n";
		return code;
	}
}
