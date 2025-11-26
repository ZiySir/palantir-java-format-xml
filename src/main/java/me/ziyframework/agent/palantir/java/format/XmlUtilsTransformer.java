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

    private static final String BASE_PACKAGE = "com/palantir";

    private static final String TARGET_CLASS = "com/palantir/javaformat/gradle/XmlUtils";

    private static final String XML_NODE_PRINTER_OLD = "groovy/util/XmlNodePrinter";

    private static final String XML_PARSER_OLD = "groovy/util/XmlParser";

    private static final Map<String, String> MAPPING = Map.of(
            XML_NODE_PRINTER_OLD, "groovy/xml/XmlNodePrinter",
            XML_PARSER_OLD, "groovy/xml/XmlParser"
    );

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (!className.startsWith(BASE_PACKAGE)) {
            return null;
        }
        // 通过字节扫描BASE_PACKAGE
        ClassReader cr = scanClasses(classfileBuffer);
        if (cr == null) {
            return null;
        }
        System.out.println(">>> [Agent] Transforming class: " + className);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // 进行类引用替换操作
        SimpleRemapper remapper = new SimpleRemapper(Opcodes.ASM9, MAPPING);
        ClassVisitor cv = new ClassRemapper(cw, remapper);

        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }

    /**
     * 扫描是否存在 groovy/util/XmlNodePrinter 或 groovy/util/XmlParser
     */
    private ClassReader scanClasses(byte[] classfile) {
        ClassReader cr = new ClassReader(classfile);
        int constantPoolCount = cr.getItemCount();

        // 遍历常量池查找目标类引用
        for (int i = 1; i < constantPoolCount; i++) {
            int itemOffset = cr.getItem(i);
            if (itemOffset == 0) {
                continue;
            }

            // 检查是否为 CONSTANT_Class_info (tag = 7)
            if (classfile[itemOffset - 1] == 7) {
                // 获取类名索引 (CONSTANT_Utf8_info)
                int nameIndex = cr.readUnsignedShort(itemOffset);
                String className = cr.readUTF8(cr.getItem(nameIndex), new char[cr.getMaxStringLength()]);

                // 检查是否为目标类
                if (XML_NODE_PRINTER_OLD.equals(className) || XML_PARSER_OLD.equals(className)) {
                    return cr;
                }
            }
        }
        return null;
    }
}
