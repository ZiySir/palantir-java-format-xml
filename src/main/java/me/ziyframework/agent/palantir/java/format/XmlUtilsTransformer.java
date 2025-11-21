package me.ziyframework.agent.palantir.java.format;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
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

    private static final Map<String, String> MAPPING = Map.of("groovy.util.XmlNodePrinter", "groovy.xml.XmlNodePrinter",
            "groovy.util.XmlParser", "groovy.util.XmlParser");

    private static final SimpleRemapper REMAPPER = new SimpleRemapper(Opcodes.ASM9, MAPPING);


    private static final String TARGET_CLASS_DOT = "com.palantir.javaformat.gradle.XmlUtils";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null || !className.equals(TARGET_CLASS_SLASH)) {
            return null;
        }

        System.out.println(">>> [XmlUtilsTransformer] 检测到目标类: " + className);

        // 使用 ASM 进行类重映射
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassRemapper classRemapper = new ClassRemapper(classWriter, REMAPPER);

        classReader.accept(classRemapper, 0);

        System.out.println(">>> [XmlUtilsTransformer] 已完成对 " + className + " 的转换");

        return classWriter.toByteArray();
    }
}