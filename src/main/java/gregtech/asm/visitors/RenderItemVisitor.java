package gregtech.asm.visitors;

import gregtech.api.GTValues;
import gregtech.asm.util.ObfMapping;

import net.minecraftforge.fml.common.Loader;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class RenderItemVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/RenderItem";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_180453_a",
            "(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V").toRuntime(); // renderItemOverlayIntoGUI

    public static void transform(Iterator<MethodNode> methods) {
        while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (TARGET_METHOD.matches(m)) {
                InsnList callRenderLampOverlay = new InsnList();
                callRenderLampOverlay.add(new VarInsnNode(ALOAD, 2));
                callRenderLampOverlay.add(new VarInsnNode(ILOAD, 3));
                callRenderLampOverlay.add(new VarInsnNode(ILOAD, 4));
                callRenderLampOverlay.add(new MethodInsnNode(INVOKESTATIC, "gregtech/asm/hooks/RenderItemHooks",
                        "renderLampOverlay", "(Lnet/minecraft/item/ItemStack;II)V", false));

                boolean enderCoreLoaded = Loader.instance().getIndexedModList().containsKey(GTValues.MODID_ECORE);

                // do not conflict with EnderCore's changes, which already do what we need
                InsnList callRenderElectricBar;
                if (!enderCoreLoaded) {
                    callRenderElectricBar = new InsnList();
                    callRenderElectricBar.add(new VarInsnNode(ALOAD, 2));
                    callRenderElectricBar.add(new VarInsnNode(ILOAD, 3));
                    callRenderElectricBar.add(new VarInsnNode(ILOAD, 4));
                    callRenderElectricBar.add(new MethodInsnNode(INVOKESTATIC, "gregtech/asm/hooks/RenderItemHooks",
                            "renderElectricBar", "(Lnet/minecraft/item/ItemStack;II)V", false));
                } else {
                    callRenderElectricBar = null;
                }

                boolean ifne = false, l2 = false, renderLampOverlayApplied = false;

                boolean primed, onFrame, renderElectricBarApplied;
                primed = onFrame = renderElectricBarApplied = enderCoreLoaded;
                Label target = null;

                for (int i = 0; i < m.instructions.size(); i++) {
                    AbstractInsnNode next = m.instructions.get(i);

                    if (!ifne) {
                        if (next.getOpcode() == IFNE) {
                            ifne = true;
                        }
                        continue;
                    }

                    if (!l2) {
                        if (next instanceof LabelNode) {
                            l2 = true;
                            m.instructions.insert(next, callRenderLampOverlay);
                        }
                        continue;
                    }

                    if (!renderLampOverlayApplied) {
                        if (next instanceof FrameNode) {
                            m.instructions.insert(next, callRenderLampOverlay);
                            renderLampOverlayApplied = true;
                        }
                        continue;
                    }

                    if (renderElectricBarApplied) {
                        break;
                    }

                    if (!primed) {
                        if (next.getOpcode() == INVOKEVIRTUAL &&
                                next instanceof MethodInsnNode &&
                                "showDurabilityBar".equals(((MethodInsnNode) next).name)) {
                            primed = true;
                        }
                        continue;
                    }

                    if (target == null) {
                        if (next.getOpcode() == IFEQ && next instanceof JumpInsnNode) {
                            target = ((JumpInsnNode) next).label.getLabel();
                        }
                        continue;
                    }

                    if (!onFrame) {
                        if (next instanceof LabelNode && ((LabelNode) next).getLabel() == target) {
                            onFrame = true;
                        }
                        continue;
                    }

                    if (next instanceof FrameNode) {
                        m.instructions.insert(next, callRenderElectricBar);
                        renderElectricBarApplied = true;
                        break;
                    }
                }
                if (!renderElectricBarApplied) {
                    m.instructions.insert(callRenderElectricBar);
                }
                break;
            }
        }
    }
}
