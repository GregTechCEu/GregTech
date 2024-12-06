package gregtech.mixins.mui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.cleanroommc.modularui.ClientEventHandler;
import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientEventHandler.class, remap = false)
@SuppressWarnings("UnstableApiUsage")
public abstract class ClientEventHandlerMixin {

    @Redirect(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleMouseInput()V"))
    private static void fixMouseInput(GuiScreen instance) {
        int button = Mouse.getEventButton();
        if (instance instanceof IMuiScreen screen && instance instanceof GuiScreenAccessor acc) {
            if (Mouse.getEventButtonState()) {
                acc.setEventButton(button);
                acc.setLastMouseEvent(Minecraft.getSystemTime());
                screen.getScreen().onMousePressed(button);

            } else if (button != -1) {
                acc.setEventButton(-1);
                screen.getScreen().onMouseRelease(button);

            } else if (acc.getEventButton() != -1 && acc.getLastMouseEvent() > 0L) {
                long l = Minecraft.getSystemTime() - acc.getLastMouseEvent();
                screen.getScreen().onMouseDrag(button, l);
            }
        }
    }

    private static Character lastChar = null;

    @Redirect(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$KeyboardInputEvent$Pre;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
    private static void fixKeyInput(GuiScreen instance) {
        if (instance instanceof IMuiScreen screen && instance instanceof GuiScreenAccessor acc) {
            char c0 = Keyboard.getEventCharacter();
            int key = Keyboard.getEventKey();
            boolean state = Keyboard.getEventKeyState();
            if (state) {
                lastChar = c0;
                screen.getScreen().onKeyPressed(c0, key);
                // return doAction(muiScreen, ms -> ms.onKeyPressed(c0, key)) || keyTyped(mcScreen, c0, key);
            } else {
                // releasing a key
                // for some reason when you press E after joining a world the button will not trigger the press event,
                // but ony the release event, causing this to be null
                if (lastChar == null) return;
                // when the key is released, the event char is empty
                screen.getScreen().onKeyRelease(lastChar, key);
                // if (doAction(muiScreen, ms -> ms.onKeyRelease(lastChar, key))) return true;
                // if (key == 0 && c0 >= ' ') {
                // return keyTyped(mcScreen, c0, key);
                // }
            }
        }
    }
}
