package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.RichTextCompiler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// todo remove once fixed in mui2
@Mixin(value = RichTextCompiler.class, remap = false)
public abstract class TextCompilerMixin {

    @Shadow
    protected abstract void newLine();

    @WrapOperation(method = "compile",
                   at = @At(value = "INVOKE",
                            target = "Lcom/cleanroommc/modularui/drawable/text/RichTextCompiler;compileString(Ljava/lang/String;)V"))
    public void fixString(RichTextCompiler instance, String c, Operation<Void> original) {
        String[] lines = c.replace("\\n", "/n").split("/n");
        for (int i = 0; i < lines.length; i++) {
            original.call(instance, lines[i]);
            if (i + 1 != lines.length) newLine();
        }
    }
}
