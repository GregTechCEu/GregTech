package gregtech.mixins.minecraft;

import com.cleanroommc.bogosorter.core.mixin.MixinMinecraft;

import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.net.URL;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "processKeyF3", at = @At("HEAD"))
    public void addGregTechDebugMessage(int auxKey, CallbackInfoReturnable<Boolean> cir) {
        if (auxKey == Keyboard.KEY_H && !Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI()
                    .printChatMessage(new TextComponentTranslation("gregtech.debug.f3_h.enabled"));
        }
    }

    @Redirect(method = "processKeyBinds",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/entity/player/InventoryPlayer;currentItem:I",
                       opcode = Opcodes.PUTFIELD))
    public void interceptHotbarKeybindsForToolbelt(InventoryPlayer inventoryPlayer, int i) {
        if (!ConfigHolder.client.toolbeltConfig.enableToolbeltKeypressCapture) return;
        if (inventoryPlayer.player.isSneaking()) {
            ItemStack stack = inventoryPlayer.player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
                toolbelt.changeSelectedToolHotkey(i, stack);
                return;
            }
        }
        inventoryPlayer.currentItem = i;
    }
}
