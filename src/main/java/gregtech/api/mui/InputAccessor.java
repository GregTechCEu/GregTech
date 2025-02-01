package gregtech.api.mui;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.viewport.LocatedWidget;

// todo remove on next mui2 update
// this can't be a true accessor because of illegal classloading
public interface InputAccessor {

    boolean held();

    void held(boolean held);

    void timeHeld(long a);

    LocatedWidget lastPressed();

    void lastPressed(LocatedWidget last);

    void lastButton(int b);

    void addInteractable(Interactable i);
}
