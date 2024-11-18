package com.akuchen.trace.agent.enhance;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class RestTemplateClassVisitor extends ClassVisitor {

	public static String PREFIX="[trace-rpc][{}]【request={}】【response={}】[cost:{}ms]";

	public RestTemplateClassVisitor(int api, ClassVisitor classVisitor) {
		super(api, classVisitor);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
		if (name.equals("doExecute")) {
			return new DoExecuteMethodVisitor(api, mv);
		}
		return mv;
	}


	public static class DoExecuteMethodVisitor extends MethodVisitor {
		public DoExecuteMethodVisitor(int api, MethodVisitor methodVisitor) {
			super(api, methodVisitor);
		}

		@Override
		public void visitCode() {

			super.visitCode();
		}

		@Override
		public void visitInsn(int opcode) {


			//log.info(PREFIX,classmethod,JSON.toJSONString(request, SerializerFeature.DisableCircularReferenceDetect),JSON.toJSONString(resonse, SerializerFeature.DisableCircularReferenceDetect));
		}
	}
}

