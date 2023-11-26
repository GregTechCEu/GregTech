package gregtech.asm.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class MinecraftHooks {

    public static void sendF3HMessage(int keyCode) {
        if (keyCode == Keyboard.KEY_H && Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI()
                    .printChatMessage(new TextComponentTranslation("gregtech.debug.f3_h.enabled"));
        }
    }
}
