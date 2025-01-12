package gregtech.api.mui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.widgets.ToggleButton;

import java.util.function.Consumer;

public interface StateOverlay {

    StateOverlay overlay(boolean selected, IDrawable... overlay);

    StateOverlay hoverOverlay(boolean selected, IDrawable... overlay);

    static ToggleButton cast(ToggleButton button, Consumer<StateOverlay> function) {
        function.accept((StateOverlay) button);
        return button;
    }

    static ToggleButton create(Consumer<StateOverlay> function) {
        return cast(new ToggleButton(), function);
    }
}
