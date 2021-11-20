package gregtech.core.util;

import org.objectweb.asm.MethodVisitor;

public abstract class SafeMethodVisitor extends MethodVisitor {

    private boolean patchedSuccessfully = false;

    public SafeMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv);
    }

    protected void markPatchedSuccessfully() {
        this.patchedSuccessfully = true;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (!patchedSuccessfully) {
            throw new RuntimeException("Patching failed, patch code wasn't injected " + getInjectTargetString());
        }
    }

    protected abstract String getInjectTargetString();
}
