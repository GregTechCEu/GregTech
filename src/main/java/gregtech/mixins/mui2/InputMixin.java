package gregtech.mixins.mui2;

import gregtech.api.mui.InputAccessor;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// todo remove on next mui2 update
@Mixin(targets = "com.cleanroommc.modularui.screen.ModularPanel$Input", remap = false)
public abstract class InputMixin implements InputAccessor {

    @Shadow
    private boolean held;

    @Shadow
    private long timeHeld;

    @Shadow
    private @Nullable LocatedWidget lastPressed;

    @Shadow
    private int lastButton;

    @Shadow
    protected abstract void addAcceptedInteractable(Interactable interactable);

    @Override
    public boolean held() {
        return this.held;
    }

    @Override
    public void held(boolean held) {
        this.held = held;
    }

    @Override
    public void timeHeld(long a) {
        timeHeld = a;
    }

    @Override
    public LocatedWidget lastPressed() {
        return this.lastPressed;
    }

    @Override
    public void lastPressed(LocatedWidget last) {
        this.lastPressed = last;
    }

    @Override
    public void lastButton(int b) {
        this.lastButton = b;
    }

    @Override
    public void addInteractable(Interactable i) {
        addAcceptedInteractable(i);
    }
}
