package gregtech.mixins.minecraft;

import gregtech.api.GTValues;
import gregtech.asm.hooks.RenderItemHooks;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import org.spongepowered.asm.mixin.Mixin;
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
            RenderItemHooks.renderLampOverlay(stack, xPosition, yPosition);
        }
    }

    @Inject(method = "renderItemOverlayIntoGUI",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/client/Minecraft;getMinecraft()Lnet/minecraft/client/Minecraft;",
                     shift = At.Shift.BEFORE,
                     ordinal = 0))
    public void showDurabilityBarMixin(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text,
                                       CallbackInfo ci) {
        if (!Loader.isModLoaded(GTValues.MODID_ECORE)) {
            RenderItemHooks.renderElectricBar(stack, xPosition, yPosition);
        }
    }
}
