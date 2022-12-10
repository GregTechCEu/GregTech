package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class RenderItemVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/RenderItem";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_180453_a", "(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V").toRuntime(); // renderItemOverlayIntoGUI

    public static void transform(Iterator<MethodNode> methods) {
        while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (TARGET_METHOD.matches(m)) {
                InsnList toAdd = new InsnList();
                toAdd.add(new VarInsnNode(ALOAD, 2));
                toAdd.add(new VarInsnNode(ILOAD, 3));
                toAdd.add(new VarInsnNode(ILOAD, 4));
                toAdd.add(new MethodInsnNode(INVOKESTATIC, "gregtech/asm/hooks/RenderItemHooks", "renderElectricBar", "(Lnet/minecraft/item/ItemStack;II)V", false));

                boolean primed = false, onFrame = false, applied = false;
                Label target = null;
                for (int i = 0; i < m.instructions.size(); i++) {
                    AbstractInsnNode next = m.instructions.get(i);

                    if (!primed && target == null && next.getOpcode() == INVOKEVIRTUAL && next instanceof MethodInsnNode) {
                        if ("showDurabilityBar".equals(((MethodInsnNode) next).name)) {
                            primed = true;
                        }
                    }

                    if (primed && next.getOpcode() == IFEQ && next instanceof JumpInsnNode) {
                        target = ((JumpInsnNode) next).label.getLabel();
                        primed = false;
                    }

                    if (target != null && next instanceof LabelNode && ((LabelNode) next).getLabel() == target) {
                        onFrame = true;
                        continue;
                    }

                    if (onFrame && next instanceof FrameNode) {
                        m.instructions.insert(next, toAdd);
                        applied = true;
                        break;
                    }
                }
                if (!applied) {
                    m.instructions.insert(toAdd);
                }
                break;
            }
        }
    }
}
