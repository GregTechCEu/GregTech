package gregtech.mixins.mui2;

import gregtech.api.mui.LocaleAccessor;

import com.cleanroommc.modularui.drawable.text.BaseKey;
import com.cleanroommc.modularui.drawable.text.LangKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// todo remove in next mui2 version
@Mixin(value = LangKey.class, remap = false)
public abstract class LangKeyMixin extends BaseKey {

    @Redirect(method = "getFormatted",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    public String getTranslateKey(String translateKey, Object[] parameters) {
        return LocaleAccessor.getRawKey(translateKey).replace("\\n", "\n");
    }
}
