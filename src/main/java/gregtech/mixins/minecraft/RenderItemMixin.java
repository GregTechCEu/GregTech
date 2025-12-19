package gregtech.mixins.minecraft;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.util.Mods;
import gregtech.client.renderer.handler.LampItemOverlayRenderer;
import gregtech.client.utils.ToolChargeBarRenderer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    // The easy part of translating the item render stuff
    @Inject(method = "renderItemOverlayIntoGUI", at = @At(value = "HEAD"))
    private void renderItemOverlayIntoGUIInject(FontRenderer fr, ItemStack stack, int xPosition, int yPosition,
                                                String text, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            gregTechCEu$renderLampOverlay(stack, xPosition, yPosition);
        }
    }

    @Inject(method = "renderItemOverlayIntoGUI",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/client/Minecraft;getMinecraft()Lnet/minecraft/client/Minecraft;",
                     shift = At.Shift.BEFORE,
                     ordinal = 0))
    public void showDurabilityBarMixin(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text,
                                       CallbackInfo ci) {
        if (!Mods.EnderCore.isModLoaded()) {
            gregTechCEu$renderElectricBar(stack, xPosition, yPosition);
        }
    }

    @Unique
    private static void gregTechCEu$renderElectricBar(@NotNull ItemStack stack, int xPosition, int yPosition) {
        if (stack.getItem() instanceof IGTTool iGTTool) {
            ToolChargeBarRenderer.renderBarsTool(iGTTool, stack, xPosition, yPosition);
        } else if (stack.getItem() instanceof MetaItem<?>metaItem) {
            ToolChargeBarRenderer.renderBarsItem(metaItem, stack, xPosition, yPosition);
        }
    }

    @Unique
    private static void gregTechCEu$renderLampOverlay(@NotNull ItemStack stack, int xPosition, int yPosition) {
        LampItemOverlayRenderer.OverlayType overlayType = LampItemOverlayRenderer.getOverlayType(stack);
        if (overlayType != LampItemOverlayRenderer.OverlayType.NONE) {
            LampItemOverlayRenderer.renderOverlay(overlayType, xPosition, yPosition);
        }
    }
}
