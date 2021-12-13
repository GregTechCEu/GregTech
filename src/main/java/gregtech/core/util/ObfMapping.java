package gregtech.core.util;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @apiNote codechicken.asm.ObfMapping
 */
@SuppressWarnings({"unused", "UnstableApiUsage"})
public class ObfMapping extends Remapper {

    public static final ObfRemapper obfMapper = new ObfRemapper();
    public static Remapper mcpMapper = null;
    public static final boolean obfuscated;

    public String s_owner;
    public String s_name;
    public String s_desc;

    public static void loadMCPRemapper() {
        if (mcpMapper == null) {
            mcpMapper = new MCPRemapper();
        }
    }

    public ObfMapping(String owner) {
        this(owner, "", "");
    }

    public ObfMapping(String owner, String name) {
        this(owner, name, "");
    }

    public ObfMapping(String owner, String name, String desc) {
        this.s_owner = owner;
        this.s_name = name;
        this.s_desc = desc;

        if (s_owner.contains(".")) {
            throw new IllegalArgumentException(s_owner);
        }
    }

    public ObfMapping(ObfMapping descmap, String subclass) {
        this(subclass, descmap.s_name, descmap.s_desc);
    }

    public static ObfMapping fromDesc(String s) {
        int lastDot = s.lastIndexOf('.');
        if (lastDot < 0) {
            return new ObfMapping(s, "", "");
        }
        int sep = s.indexOf('(');//methods
        int sep_end = sep;
        if (sep < 0) {
            sep = s.indexOf(' ');//some stuffs
            sep_end = sep + 1;
        }
        if (sep < 0) {
            sep = s.indexOf(':');//fields
            sep_end = sep + 1;
        }
        if (sep < 0) {
            return new ObfMapping(s.substring(0, lastDot), s.substring(lastDot + 1), "");
        }

        return new ObfMapping(s.substring(0, lastDot), s.substring(lastDot + 1, sep), s.substring(sep_end));
    }

    public ObfMapping subclass(String subclass) {
        return new ObfMapping(this, subclass);
    }

    public boolean matches(MethodNode node) {
        return s_name.equals(node.name) && s_desc.equals(node.desc);
    }

    public boolean matches(MethodInsnNode node) {
        return s_owner.equals(node.owner) && s_name.equals(node.name) && s_desc.equals(node.desc);
    }

    public AbstractInsnNode toInsn(int opcode) {
        if (isClass()) {
            return new TypeInsnNode(opcode, s_owner);
        } else if (isMethod()) {
            return new MethodInsnNode(opcode, s_owner, s_name, s_desc, opcode == Opcodes.INVOKEINTERFACE);
        } else {
            return new FieldInsnNode(opcode, s_owner, s_name, s_desc);
        }
    }

    public void visitTypeInsn(MethodVisitor mv, int opcode) {
        mv.visitTypeInsn(opcode, s_owner);
    }

    public void visitMethodInsn(MethodVisitor mv, int opcode) {
        mv.visitMethodInsn(opcode, s_owner, s_name, s_desc, opcode == Opcodes.INVOKEINTERFACE);
    }

    public void visitFieldInsn(MethodVisitor mv, int opcode) {
        mv.visitFieldInsn(opcode, s_owner, s_name, s_desc);
    }

    public MethodVisitor visitMethod(ClassVisitor visitor, int access, String[] exceptions) {
        return visitor.visitMethod(access, s_name, s_desc, null, exceptions);
    }

    public FieldVisitor visitField(ClassVisitor visitor, int access, Object value) {
        return visitor.visitField(access, s_name, s_desc, null, value);
    }

    public boolean isClass(String name) {
        return name.replace('.', '/').equals(s_owner);
    }

    public boolean matches(String name, String desc) {
        return s_name.equals(name) && s_desc.equals(desc);
    }

    public boolean matches(FieldNode node) {
        return s_name.equals(node.name) && s_desc.equals(node.desc);
    }

    public boolean matches(FieldInsnNode node) {
        return s_owner.equals(node.owner) && s_name.equals(node.name) && s_desc.equals(node.desc);
    }

    public String javaClass() {
        return s_owner.replace('/', '.');
    }

    public String methodDesc() {
        return s_owner + "." + s_name + s_desc;
    }

    public String fieldDesc() {
        return s_owner + "." + s_name + ":" + s_desc;
    }

    public boolean isClass() {
        return s_name.length() == 0;
    }

    public boolean isMethod() {
        return s_desc.contains("(");
    }

    public boolean isField() {
        return !isClass() && !isMethod();
    }

    public ObfMapping map(Remapper mapper) {
        if (mapper == null) {
            return this;
        }

        if (isMethod()) {
            s_name = mapper.mapMethodName(s_owner, s_name, s_desc);
        } else if (isField()) {
            s_name = mapper.mapFieldName(s_owner, s_name, s_desc);
        }

        s_owner = mapper.mapType(s_owner);

        if (isMethod()) {
            s_desc = mapper.mapMethodDesc(s_desc);
        } else if (s_desc.length() > 0) {
            s_desc = mapper.mapDesc(s_desc);
        }

        return this;
    }

    public ObfMapping toRuntime() {
        map(mcpMapper);
        return this;
    }

    public ObfMapping toClassloading() {
        if (!obfuscated) {
            map(mcpMapper);
        } else if (obfMapper.isObf(s_owner)) {
            map(obfMapper);
        }
        return this;
    }

    public ObfMapping copy() {
        return new ObfMapping(s_owner, s_name, s_desc);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObfMapping)) {
            return false;
        }

        ObfMapping desc = (ObfMapping) obj;
        return s_owner.equals(desc.s_owner) && s_name.equals(desc.s_name) && s_desc.equals(desc.s_desc);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(s_desc, s_name, s_owner);
    }

    @Override
    public String toString() {
        if (s_name.length() == 0) {
            return "[" + s_owner + "]";
        }
        if (s_desc.length() == 0) {
            return "[" + s_owner + "." + s_name + "]";
        }
        return "[" + (isMethod() ? methodDesc() : fieldDesc()) + "]";
    }

    public static class ObfRemapper extends Remapper {

        private final HashMap<String, String> fields = new HashMap<>();
        private final HashMap<String, String> funcs = new HashMap<>();

        @SuppressWarnings("unchecked")
        public ObfRemapper() {
            try {
                Field rawFieldMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawFieldMaps");
                Field rawMethodMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawMethodMaps");
                rawFieldMapsField.setAccessible(true);
                rawMethodMapsField.setAccessible(true);
                Map<String, Map<String, String>> rawFieldMaps = (Map<String, Map<String, String>>) rawFieldMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);
                Map<String, Map<String, String>> rawMethodMaps = (Map<String, Map<String, String>>) rawMethodMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);

                if (rawFieldMaps == null) {
                    throw new IllegalStateException("gregtech.core.util.ObfMapping loaded too early. Make sure all references are in or after the asm transformer load stage");
                }

                for (Map<String, String> map : rawFieldMaps.values()) {
                    for (Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue().startsWith("field")) {
                            fields.put(entry.getValue(), entry.getKey().substring(0, entry.getKey().indexOf(':')));
                        }
                    }
                }

                for (Map<String, String> map : rawMethodMaps.values()) {
                    for (Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue().startsWith("func")) {
                            funcs.put(entry.getValue(), entry.getKey().substring(0, entry.getKey().indexOf('(')));
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String s = funcs.get(name);
            return s == null ? name : s;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String s = fields.get(name);
            return s == null ? name : s;
        }

        @Override
        public String map(String typeName) {
            return FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
        }

        public String unmap(String typeName) {
            return FMLDeobfuscatingRemapper.INSTANCE.map(typeName);
        }

        public boolean isObf(String typeName) {
            return !map(typeName).equals(typeName) || !unmap(typeName).equals(typeName);
        }
    }

    public static class MCPRemapper extends Remapper implements LineProcessor<Void> {

        private final HashMap<String, String> fields = new HashMap<>();
        private final HashMap<String, String> funcs = new HashMap<>();

        public MCPRemapper() {
            File[] mappings = getConfFiles();
            try {
                Resources.readLines(mappings[1].toURI().toURL(), Charsets.UTF_8, this);
                Resources.readLines(mappings[2].toURI().toURL(), Charsets.UTF_8, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static File[] getConfFiles() {
            // check for GradleStart system vars
            File notchSrg = new File(System.getProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg"));
            File csvDir = new File(System.getProperty("net.minecraftforge.gradle.GradleStart.csvDir"));

            if (notchSrg.exists() && csvDir.exists()) {
                File fieldCsv = new File(csvDir, "fields.csv");
                File methodCsv = new File(csvDir, "methods.csv");

                if (notchSrg.exists() && fieldCsv.exists() && methodCsv.exists()) {
                    return new File[]{notchSrg, fieldCsv, methodCsv};
                }
            }

            throw new RuntimeException("Failed to grab mappings from GradleStart args.");
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String s = funcs.get(name);
            return s == null ? name : s;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String s = fields.get(name);
            return s == null ? name : s;
        }

        @Override
        public boolean processLine(@Nonnull String line) {
            int i = line.indexOf(',');
            String srg = line.substring(0, i);
            int i2 = i + 1;
            i = line.indexOf(',', i2);
            String mcp = line.substring(i2, i);
            (srg.startsWith("func") ? funcs : fields).put(srg, mcp);
            return true;
        }

        @Override
        public Void getResult() {
            return null;
        }

    }

    static {
        boolean obf = true;
        try {
            obf = Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;
        } catch (Exception ignored) {
        }
        obfuscated = obf;
        if (!obf) {
            loadMCPRemapper();
        }
    }

}
