package gregtech.mixins.mui2;

import com.cleanroommc.modularui.drawable.text.BaseKey;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

// all this mixin does is switch newlines to the expected format
@Mixin(value = LangKey.class, remap = false)
public abstract class LangKeyMixin extends BaseKey {

    @Shadow
    private long time;
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

    @Inject(method = "<init>(Ljava/util/function/Supplier;Ljava/util/function/Supplier;)V",
            at = @At(value = "RETURN"))
    private void setTimeToNegativeOne(@NotNull Supplier<String> keySupplier, @NotNull Supplier<Object[]> argsSupplier,
                                      CallbackInfo ci) {
        time = -1;
    }
}
