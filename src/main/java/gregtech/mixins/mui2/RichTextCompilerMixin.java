package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.RichTextCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = RichTextCompiler.class, remap = false)
public class RichTextCompilerMixin {

    @ModifyArg(method = "trimRight",
               at = @At(value = "INVOKE",
                        target = "Ljava/lang/String;substring(II)Ljava/lang/String;"),
               index = 1)
    private static int fixTrim(int endIndex) {
        return endIndex + 1;
    }
}
