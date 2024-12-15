package gregtech.mixins.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "processKeyF3", at = @At("HEAD"))
    public void addGregTechDebugMessage(int auxKey, CallbackInfoReturnable<Boolean> cir) {
        if (auxKey == Keyboard.KEY_H && !Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI()
                    .printChatMessage(new TextComponentTranslation("gregtech.debug.f3_h.enabled"));
        }
    }
}
