package gregtech.mixins.forge;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreIngredient.class)
public abstract class OreIngredientMixin {

    @Inject(method = "apply(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void checkToolbelt(ItemStack input, CallbackInfoReturnable<Boolean> cir) {
        if (ItemGTToolbelt.checkIngredientAgainstToolbelt(input, (OreIngredient) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
