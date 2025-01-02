package gregtech.mixins.jei;

import net.minecraft.client.Minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IngredientGridWithNavigation.class, remap = false)
public class IngredientGridMixin {

    @Shadow
    @Final
    private GuiScreenHelper guiScreenHelper;

    @WrapOperation(method = "drawTooltips",
                   at = @At(value = "INVOKE",
                            target = "Lmezz/jei/gui/overlay/IngredientGrid;drawTooltips(Lnet/minecraft/client/Minecraft;II)V"))
    private void considerExclusions(IngredientGrid instance, Minecraft minecraft, int mouseX, int mouseY,
                                    Operation<Void> original) {
        if (!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY))
            original.call(instance, minecraft, mouseX, mouseY);
    }
}
