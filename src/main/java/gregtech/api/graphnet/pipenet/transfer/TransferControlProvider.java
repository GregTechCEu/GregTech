package gregtech.api.graphnet.pipenet.transfer;

import org.jetbrains.annotations.Nullable;

public interface TransferControlProvider {

    <T> @Nullable T getControllerForControl(TransferControl<T> control);
}
