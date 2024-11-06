package gregtech.api.mui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.widgets.ToggleButton;

public interface StateOverlay {

    ToggleButton overlay(boolean selected, IDrawable... overlay);

    ToggleButton hoverOverlay(boolean selected, IDrawable... overlay);

    static StateOverlay cast(ToggleButton button) {
        return (StateOverlay) button;
    }
}
