package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TileEntityVisitor extends MethodVisitor implements Opcodes {

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

    public TileEntityVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKEVIRTUAL && NEW_INSTANCE.matches(name, desc)) {
            mv.visitVarInsn(Opcodes.POP, 4); // unload oclass
            mv.visitVarInsn(Opcodes.ALOAD, 3); // load s
            HOOK.visitMethodInsn(this, INVOKESTATIC); // call hook
            mv.visitVarInsn(Opcodes.ASTORE, 2); // store in tileentity

            // label for vanilla logic
            Label elseLabel = new Label();
            Label endLabel = new Label();

            // if (tileentity == null)
            mv.visitVarInsn(Opcodes.ALOAD, 2); // load tileentity
            mv.visitJumpInsn(Opcodes.IFNONNULL, elseLabel); // check null

            mv.visitVarInsn(Opcodes.ALOAD, 4); // load oclass
            NEW_INSTANCE.visitMethodInsn(mv, INVOKEVIRTUAL); // call oclass.newInstance()
            mv.visitTypeInsn(Opcodes.CHECKCAST, TARGET_CLASS_NAME); // perform implicit type cast check, required by Class#newInstance

            mv.visitVarInsn(Opcodes.ASTORE, 2); // store the result in tileentity
            mv.visitLabel(elseLabel); // complete the if block

            // go to the end of the if block
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
            mv.visitLabel(endLabel);

            // load the tile entity, so the remaining methods store it in itself
            mv.visitVarInsn(Opcodes.ALOAD, 2);

            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    // public static TileEntity create(World worldIn, NBTTagCompound compound)
    private static String targetSignature() {
        return "(" +
                "Lnet/minecraft/world/World;" + // World
                "Lnet/minecraft/nbt/NBTTagCompound;" + // NBTTagCompound
                ")Lnet/minecraft/tileentity/TileEntity;"; // return TileEntity
    }

    // public static TileEntity createMTE(String id)
    private static String hookSignature() {
        return "(" +
                "Ljava/lang/String;" + // String
                ")Lnet/minecraft/tileentity/TileEntity;"; // return TileEntity
    }
}
