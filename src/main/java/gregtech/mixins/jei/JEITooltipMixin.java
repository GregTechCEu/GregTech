package gregtech.mixins.jei;

import mezz.jei.startup.ForgeModIdHelper;
import org.spongepowered.asm.mixin.Mixin;

// TODO, Needs to apply to the fluid items in JEI
@Mixin(ForgeModIdHelper.class)
public class JEITooltipMixin {

    /*
     * @Inject(method = "addModNameToIngredientTooltip", at = @At("HEAD"), remap = false)
     * public void addTooltip(List<String> tooltip, Object ingredient, IIngredientHelper<Object> ingredientHelper,
     * CallbackInfoReturnable<List<String>> cir) {
     * if (ingredient instanceof FluidStack) {
     * List<String> formula = FluidTooltipUtil.getFluidTooltip((FluidStack) ingredient);
     * if (formula != null) {
     * for (String s : formula) {
     * if (s.isEmpty()) continue;
     * tooltip.add(s);
     * }
     * }
     * }
     * }
     */
}
