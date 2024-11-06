package gregtech.mixins.mui2;

import net.minecraftforge.client.event.GuiScreenEvent;

import com.cleanroommc.modularui.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventHandler.class, remap = false)
@SuppressWarnings("UnstableApiUsage")
public abstract class ClientEventHandlerMixin {

    @Inject(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void fixMouseInput(GuiScreenEvent.MouseInputEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onGuiInput(Lnet/minecraftforge/client/event/GuiScreenEvent$KeyboardInputEvent$Pre;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void fixKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }
}
