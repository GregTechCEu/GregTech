package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class TileEntityVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/tileentity/TileEntity";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(
            TARGET_CLASS_NAME,
            "func_190200_a", // create()
            targetSignature()
    ).toRuntime();

    private static final ObfMapping NEW_INSTANCE = new ObfMapping(
            "java/lang/Class",
            "newInstance",
            "()Ljava/lang/Object;"
    );

    private static final String HOOK_OWNER = "gregtech/asm/hooks/TileEntityHooks";
    private static final String HOOK_SIGNATURE = hookSignature();
    private static final String HOOK_METHOD_NAME = "createMTE";

    private static final ObfMapping HOOK = new ObfMapping(
            HOOK_OWNER,
            HOOK_METHOD_NAME,
            HOOK_SIGNATURE
    );

    public static void transform(@NotNull Iterator<MethodNode> methods) {
        while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (TARGET_METHOD.matches(m)) {
                InsnList insnList = new InsnList();

                insnList.add(new VarInsnNode(Opcodes.ALOAD, 3)); // load TE string id
                insnList.add(new MethodInsnNode(INVOKESTATIC, "gregtech/asm/hooks/TileEntityHooks", "createMTE", hookSignature(), false));
                insnList.add(new VarInsnNode(Opcodes.ASTORE, 2)); //store in GT tileentity

                LabelNode endLabel = new LabelNode();
                insnList.add(new LineNumberNode(124, endLabel));

                // if (tileentity != null)
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 2)); // load tileentity
                insnList.add(new JumpInsnNode(Opcodes.IFNONNULL, endLabel));
                // if (tileentity == null)
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 4)); // push oclass to the stack
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "newInstance",
                        "()Ljava/lang/Object;", false));
                //Checkcast is necessary.. really.
                insnList.add(new TypeInsnNode(CHECKCAST, "net/minecraft/tileentity/TileEntity"));

                insnList.add(new VarInsnNode(Opcodes.ASTORE, 2)); // store in tileentity
                insnList.add(endLabel);

                int al4 = 0;
                int idx = 0;

                for (int i = 0; i < m.instructions.size(); i++) {
                    AbstractInsnNode next = m.instructions.get(i);
                    if (next instanceof VarInsnNode varInsnNode) {
                        if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 4) {
                            al4++;
                            if (al4 == 2) {
                                idx = i;
                                break;
                            }
                        }
                    }
                }

                for (int i = 0; i < 4; i++) {
                    AbstractInsnNode next = m.instructions.get(idx);
                    m.instructions.remove(next);
                }

                AbstractInsnNode next = m.instructions.get(idx);

                m.instructions.insertBefore(next, insnList);
            }
        }
    }

    // public static TileEntity create(World worldIn, NBTTagCompound compound)
    private static @NotNull String targetSignature() {
        return "(" +
                "Lnet/minecraft/world/World;" + // World
                "Lnet/minecraft/nbt/NBTTagCompound;" + // NBTTagCompound
                ")Lnet/minecraft/tileentity/TileEntity;"; // return TileEntity
    }

    // public static TileEntity createMTE(String id)
    private static @NotNull String hookSignature() {
        return "(" +
                "Ljava/lang/String;" + // String
                ")Lnet/minecraft/tileentity/TileEntity;"; // return TileEntity
    }
}
