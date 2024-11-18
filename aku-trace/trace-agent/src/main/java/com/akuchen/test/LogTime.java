package com.akuchen.test;

public class LogTime {

	public static long timer;
	public void info(String string){
		timer -= System.currentTimeMillis();
		System.out.println("log:"+string);
		timer -= System.currentTimeMillis();
		System.out.println("log:"+string +"&timer:"+timer);
	}
}
