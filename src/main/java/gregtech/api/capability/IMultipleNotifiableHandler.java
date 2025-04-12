package gregtech.api.capability;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface IMultipleNotifiableHandler {

    @NotNull
    Collection<INotifiableHandler> getBackingNotifiers();
}
