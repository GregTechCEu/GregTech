package gregtech.mixins.jei;

import gregtech.api.util.FluidTooltipUtil;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.startup.ForgeModIdHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ForgeModIdHelper.class)
public class JEITooltipMixin {

    @Inject(method = "addModNameToIngredientTooltip", at = @At("HEAD"), remap = false)
    public void addTooltip(List<String> tooltip, Object ingredient, IIngredientHelper<Object> ingredientHelper,
                           CallbackInfoReturnable<List<String>> cir) {
        if (ingredient instanceof FluidStack) {
            List<String> formula = FluidTooltipUtil.getFluidTooltip((FluidStack) ingredient);
            for (String s : formula) {
                if (s.isEmpty()) continue;
                tooltip.add(s);
            }
        }
    }
}
