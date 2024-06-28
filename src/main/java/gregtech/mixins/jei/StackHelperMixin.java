package gregtech.mixins.jei;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.item.ItemStack;

import mezz.jei.startup.StackHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StackHelper.class)
public class StackHelperMixin {

    @Inject(method = "isEquivalent", at = @At(value = "RETURN", ordinal = 2), cancellable = true, remap = false)
    private void toolbeltIsEquivalent(ItemStack lhs, ItemStack rhs, CallbackInfoReturnable<Boolean> cir) {
        if (ItemGTToolbelt.checkToolAgainstToolbelt(rhs, lhs)) {
            cir.setReturnValue(true);
        }
    }
}
