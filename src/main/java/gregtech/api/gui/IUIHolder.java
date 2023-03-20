package gregtech.api.gui;

import gregtech.api.util.IDirtyNotifiable;

public interface IUIHolder extends IDirtyNotifiable {

    boolean isValid();

    boolean isRemote();

}
