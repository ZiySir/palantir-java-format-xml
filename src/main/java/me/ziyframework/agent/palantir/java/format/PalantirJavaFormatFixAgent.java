package me.ziyframework.agent.palantir.java.format;

import java.lang.instrument.Instrumentation;

/**
 * 拦截修改Palantir的Java-Format中xml问题.<br/>
 * <a href="https://github.com/palantir/palantir-java-format/pull/1478">pull request</a> <br />
 * created in 2025-11
 *
 * @author ziy
 */
public class PalantirJavaFormatFixAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(">>> [PalantirJavaFormatFixAgent] 已加载，准备修复 Groovy XmlParser 兼容性问题...");
        inst.addTransformer(new XmlFixTransformer(), true);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println(">>> [PalantirJavaFormatFixAgent] attach 已加载，准备修复 Groovy XmlParser 兼容性问题...");
        inst.addTransformer(new XmlFixTransformer(), true);
    }
}
