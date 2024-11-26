package gregtech.mixins.forge;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public class GuiIngameForgeMixin extends GuiIngame {

    private GuiIngameForgeMixin(Minecraft mcIn) {
        super(mcIn);
    }

    @Redirect(method = "renderToolHighlight",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/ScaledResolution;getScaledHeight()I"))
    private int shiftToolHighlightText(ScaledResolution res) {
        if (ConfigHolder.client.toolbeltConfig.enableToolbeltHotbarDisplay &&
                highlightingItemStack.getItem() instanceof ItemGTToolbelt)
            return res.getScaledHeight() - 31 + 6;
        else return res.getScaledHeight();
    }
}
