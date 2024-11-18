package com.akuchen.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

public class Ccb {

	public static void main(String[] args) throws IOException {
		Integer i=0;

		ClassReader classReader = new ClassReader("java.lang.Integer");
		ClassWriter classWriter = new ClassWriter(0);
		classReader.accept(classWriter, 0);
	}
}
