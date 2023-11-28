package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class DamageSourceVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/util/DamageSource";
    private static final String DAMAGE_SOURCE_OWNER = "gregtech/api/damagesources/DamageSources";

    private static final ObfMapping TARGET_METHOD_PLAYER = new ObfMapping(TARGET_CLASS_NAME, "func_76365_a",
            "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;").toRuntime();
    private static final String TARGET_SIGNATURE_PLAYER = "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/DamageSource;";
    private static final String TARGET_NAME_PLAYER = "getPlayerDamage";

    private static final ObfMapping TARGET_METHOD_MOB = new ObfMapping(TARGET_CLASS_NAME, "func_76358_a",
            "(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/util/DamageSource;").toRuntime();
    private static final String TARGET_SIGNATURE_MOB = "(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/util/DamageSource;";
    private static final String TARGET_NAME_MOB = "getMobDamage";

    public static ClassNode handleClassNode(ClassNode classNode) {
        int done = 0;
        for (MethodNode m : classNode.methods) {
            if (done == 2) break;

            // causePlayerDamage()
            if (m.name.equals(TARGET_METHOD_PLAYER.s_name) && m.desc.equals(TARGET_METHOD_PLAYER.s_desc)) {
                InsnList insns = new InsnList();
                insns.add(new VarInsnNode(ALOAD, 0));
                insns.add(new MethodInsnNode(INVOKESTATIC, DAMAGE_SOURCE_OWNER, TARGET_NAME_PLAYER,
                        TARGET_SIGNATURE_PLAYER, false));
                insns.add(new InsnNode(ARETURN));
                AbstractInsnNode first = m.instructions.getFirst();
                m.instructions.insertBefore(first, insns);
                done++;
            }

            // causeMobDamage()
            if (m.name.equals(TARGET_METHOD_MOB.s_name) && m.desc.equals(TARGET_METHOD_MOB.s_desc)) {
                InsnList insns = new InsnList();
                insns.add(new VarInsnNode(ALOAD, 0));
                insns.add(new MethodInsnNode(INVOKESTATIC, DAMAGE_SOURCE_OWNER, TARGET_NAME_MOB, TARGET_SIGNATURE_MOB,
                        false));
                insns.add(new InsnNode(ARETURN));
                AbstractInsnNode first = m.instructions.getFirst();
                m.instructions.insertBefore(first, insns);
                done++;
            }
        }
        return classNode;
    }
}
