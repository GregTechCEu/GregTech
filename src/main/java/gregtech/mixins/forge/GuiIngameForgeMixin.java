package gregtech.mixins.forge;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiIngameForge.class)
public class GuiIngameForgeMixin extends GuiIngame {

    private GuiIngameForgeMixin(Minecraft mcIn) {
        super(mcIn);
    }

    @WrapOperation(method = "renderToolHighlight",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/ScaledResolution;getScaledHeight()I"))
    private int shiftToolHighlightText(ScaledResolution resolution, Operation<Integer> op) {
        if (ConfigHolder.client.toolbeltConfig.enableToolbeltHotbarDisplay &&
                highlightingItemStack.getItem() instanceof ItemGTToolbelt)
            return op.call(resolution) - 31 + 6;
        else return op.call(resolution);
    }
}
