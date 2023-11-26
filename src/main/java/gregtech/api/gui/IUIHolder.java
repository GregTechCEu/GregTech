package gregtech.api.gui;

import gregtech.api.util.IDirtyNotifiable;

public interface IUIHolder extends IDirtyNotifiable {

    boolean isValid();

    boolean isRemote();

    /** UI color override to optionally use for whatever reason; return -1 to not override. */
    default int getUIColorOverride() {
        return -1;
    }
}
