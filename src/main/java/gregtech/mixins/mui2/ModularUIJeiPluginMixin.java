package gregtech.mixins.mui2;

import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.ModularUIJeiPlugin;
import mezz.jei.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModularUIJeiPlugin.class)
public class ModularUIJeiPluginMixin {

    // TODO: remove this mixin when the fix from Brachy makes it into a release we use
    @Inject(method = "hoveringOverIngredient", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void cancelIfCheatsOn(JeiGhostIngredientSlot<?> ingredientSlot,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (Config.isCheatItemsEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
