package gregtech.mixins.minecraft;

import gregtech.api.items.toolitem.IGTTool;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class EnchantmentCanApplyMixin {

    @ModifyReturnValue(method = "canApply", at = @At("RETURN"))
    private boolean enchantmentCanApply(boolean originalResult, @Local(ordinal = 0) ItemStack stack) {
        if (stack.getItem() instanceof IGTTool) {
            // noinspection ConstantValue
            return originalResult && stack.getItem().canApplyAtEnchantingTable(stack, ((Enchantment) (Object) this));
        }
        return originalResult;
    }
}
