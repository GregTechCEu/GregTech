package gregtech.mixins.mui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;

import com.cleanroommc.modularui.ClientEventHandler;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.ModularScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = ClientEventHandler.class, remap = false)
@SuppressWarnings("UnstableApiUsage")
public abstract class ClientEventHandlerMixin {

    @Redirect(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V"))
    private static void fixMouseInput(GuiScreen instance) {
        int button = Mouse.getEventButton();
        if (instance instanceof IMuiScreen screen && instance instanceof GuiScreenAccessor acc) {
            ModularScreen ms = screen.getScreen();
            if (Mouse.getEventButtonState()) {
                acc.setEventButton(button);
                acc.setLastMouseEvent(Minecraft.getSystemTime());
                ms.onMousePressed(button);

            } else if (button != -1) {
                acc.setEventButton(-1);
                ms.onMouseRelease(button);

            } else if (acc.getEventButton() != -1 && acc.getLastMouseEvent() > 0L) {
                long l = Minecraft.getSystemTime() - acc.getLastMouseEvent();
                ms.onMouseDrag(button, l);
            }
        }
    }

    @Inject(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V",
            at = @At("TAIL"))
    private static void fixScrollJei(GuiScreenEvent.MouseInputEvent.Pre event, CallbackInfo ci) throws IOException {
        if (!event.isCanceled())
            ClientScreenHandler.onGuiInputLow(event);
    }

    @Unique
    private static Character gregTech$lastChar = null;

    @Redirect(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$KeyboardInputEvent$Pre;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
    private static void fixKeyInput(GuiScreen instance) {
        if (instance instanceof IMuiScreen screen && instance instanceof GuiScreenAccessor acc) {
            char c0 = Keyboard.getEventCharacter();
            int key = Keyboard.getEventKey();
            boolean state = Keyboard.getEventKeyState();
            if (state) {
                gregTech$lastChar = c0;
                screen.getScreen().onKeyPressed(c0, key);
            } else {
                // releasing a key
                // for some reason when you press E after joining a world the button will not trigger the press event,
                // but only the release event, causing this to be null
                if (gregTech$lastChar == null) return;
                // when the key is released, the event char is empty
                screen.getScreen().onKeyRelease(gregTech$lastChar, key);
            }
        }
    }
}
