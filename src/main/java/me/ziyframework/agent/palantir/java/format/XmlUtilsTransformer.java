package me.ziyframework.agent.palantir.java.format;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.security.ProtectionDomain;
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

    private static final String[] TARGET_CLASS = {"com.palantir.baseline.plugins.", "com/palantir/javaformat/gradle/XmlUtils"};

    private static final Map<String, String> MAPPING = Map.of(
            "groovy/util/XmlNodePrinter", "groovy/xml/XmlNodePrinter",
            "groovy/util/XmlParser", "groovy/xml/XmlParser"
    );

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        System.out.println(">>> [Agent] Transforming class: " + className);

        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // 进行类引用替换操作
        SimpleRemapper remapper = new SimpleRemapper(Opcodes.ASM9, MAPPING);
        ClassVisitor cv = new ClassRemapper(cw, remapper);

        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }
}
