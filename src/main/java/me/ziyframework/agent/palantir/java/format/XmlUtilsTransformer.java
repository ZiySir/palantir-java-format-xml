package me.ziyframework.agent.palantir.java.format;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 将XmlUtils类的api进行迁移.
 * created in 2025-11
 *
 * @author ziy
 */
public class XmlUtilsTransformer implements ClassFileTransformer {

    private static final String TARGET_CLASS_SLASH = "com/palantir/javaformat/gradle/XmlUtils";

    private static final Map<String, String> MAPPING = Map.of(
            "groovy/util/XmlNodePrinter", "groovy/xml/XmlNodePrinter",
            "groovy/util/XmlParser", "groovy/xml/XmlParser");

    private static final SimpleRemapper REMAPPER = new SimpleRemapper(Opcodes.ASM9, MAPPING);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || !className.equals(TARGET_CLASS_SLASH)) {
            return null;
        }
        System.out.println("Loading class: " + className + " from " + loader);


        System.out.println(">>> [XmlUtilsTransformer] 检测到目标类: " + className);

        // 使用 ASM 进行类重映射
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassRemapper classRemapper = new ClassRemapper(classWriter, REMAPPER);

//        classReader.accept(classRemapper, ClassReader.EXPAND_FRAMES);

        System.out.println(">>> [XmlUtilsTransformer] 已完成对 " + className + " 的转换");

        StaticInitPrinterAdapter staticInitAdapter = new StaticInitPrinterAdapter(classRemapper);

        classReader.accept(staticInitAdapter, ClassReader.EXPAND_FRAMES);

        byte[] byteArray = classWriter.toByteArray();
        if (!Arrays.equals(classfileBuffer, byteArray)) {
            System.out.println(">>> [XmlUtilsTransformer] 字节码已修改");
        } else {
            System.out.println(">>> [XmlUtilsTransformer] 字节码未发生变化");
        }
        return byteArray;
    }

    public static class StaticInitPrinterAdapter extends ClassVisitor {

        public StaticInitPrinterAdapter(ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
        }

        @Override
        public void visitEnd() {
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();

            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(">>> [XmlUtilsTransformer] Static init block executed!");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println",
                    "(Ljava/lang/String;)V", false);

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(2, 0);
            mv.visitEnd();

            super.visitEnd();
        }
    }
}
