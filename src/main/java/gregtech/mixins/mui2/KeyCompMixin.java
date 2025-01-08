package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.BaseKey;

import com.cleanroommc.modularui.drawable.text.CompoundKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CompoundKey.class, remap = false)
public abstract class KeyCompMixin extends BaseKey {

    @Redirect(method = "get", at = @At(value = "INVOKE", target = "Lcom/cleanroommc/modularui/api/drawable/IKey;get()Ljava/lang/String;"))
    public String formatTheKeys(IKey key) {
        return key.getFormatted();
    }
}
