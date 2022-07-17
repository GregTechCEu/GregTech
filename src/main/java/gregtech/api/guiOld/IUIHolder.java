package gregtech.api.guiOld;

import gregtech.api.util.IDirtyNotifiable;

public interface IUIHolder extends IDirtyNotifiable {

    boolean isValid();

    boolean isRemote();

    void markAsDirty();

}
