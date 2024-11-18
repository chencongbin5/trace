package com.akuchen.trace.agent;

import com.akuchen.trace.agent.enhance.RestTemplateTransformer;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TraceAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		//1.RestTemplate 增强
		inst.addTransformer(new RestTemplateTransformer());

	}
}
