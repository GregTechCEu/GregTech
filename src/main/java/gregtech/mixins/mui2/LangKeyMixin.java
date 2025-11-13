package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.BaseKey;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// all this mixin does is switch newlines to the expected format
@Mixin(value = LangKey.class, remap = false)
public abstract class LangKeyMixin extends BaseKey {

    @ModifyExpressionValue(method = "getFormatted",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/cleanroommc/modularui/drawable/text/FontRenderHelper;formatArgs([Ljava/lang/Object;Lcom/cleanroommc/modularui/drawable/text/FormattingState;Ljava/lang/String;Z)Ljava/lang/String;"))
    public String getTranslateKey(String original) {
        return original
                .replace("\\n", "\n")
                .replace("/n", "\n");
    }

    @ModifyExpressionValue(method = "get",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    public String switchNewLines(String original) {
        return original.replace("/n", "\n");
    }
}
