package com.akuchen.trace.agent.enhance;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class RestTemplateTransformer implements ClassFileTransformer {
	@Override
	public byte[] transform(ClassLoader loader,
							String className,
							Class<?> classBeingRedefined,
							ProtectionDomain protectionDomain,
							byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.equals("org/springframework/web/client/RestTemplate")) {
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
			RestTemplateClassVisitor cv = new RestTemplateClassVisitor(Opcodes.ASM5, cw);
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		return classfileBuffer;
	}
}
