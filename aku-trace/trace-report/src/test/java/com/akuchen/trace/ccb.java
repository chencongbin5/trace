package com.akuchen.trace;

import com.akuchen.trace.report.log.TraceLogger;

import java.util.concurrent.atomic.AtomicReference;

public class ccb {

	public static void main(String[] args) {
		AtomicReference<String> headerAto=new AtomicReference<>();
		headerAto.set(null);
		System.out.println(headerAto.get());
	}
}
