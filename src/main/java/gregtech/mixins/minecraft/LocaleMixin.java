package gregtech.mixins.minecraft;

import gregtech.api.mui.LocaleAccessor;

import net.minecraft.client.resources.Locale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Locale.class)
public abstract class LocaleMixin implements LocaleAccessor {

    @Shadow
    Map<String, String> properties;

    @Override
    public String gregtech$getRawKey(String s) {
        return this.properties.get(s);
    }
}
