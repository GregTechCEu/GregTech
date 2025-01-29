package gregtech.mixins.minecraft;

import gregtech.api.mui.LocaleAccessor;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// todo remove in next mui2 version
@Mixin(I18n.class)
public abstract class L18nMixin {

    @Inject(method = "setLocale", at = @At("HEAD"))
    private static void getLocale(Locale i18nLocaleIn, CallbackInfo ci) {
        LocaleAccessor.setLocale(i18nLocaleIn);
    }
}
