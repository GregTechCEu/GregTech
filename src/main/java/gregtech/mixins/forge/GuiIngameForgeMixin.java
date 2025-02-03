package gregtech.mixins.forge;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiIngameForge.class)
public class GuiIngameForgeMixin extends GuiIngame {

    private GuiIngameForgeMixin(Minecraft mcIn) {
        super(mcIn);
    }

    @ModifyExpressionValue(method = "renderToolHighlight",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/gui/ScaledResolution;getScaledHeight()I"))
    private int shiftToolHighlightText(int y) {
        if (ConfigHolder.client.toolbeltConfig.enableToolbeltHotbarDisplay &&
                highlightingItemStack.getItem() instanceof ItemGTToolbelt)
            return y - 31 + 6;
        else return y;
    }
}
