package gregtech.common.pipelike.net.optical;

import gregtech.api.capability.data.IDataAccess;
import gregtech.api.capability.data.query.DataQueryObject;
import gregtech.api.graphnet.pipenet.transfer.TransferControl;
import gregtech.api.graphnet.pipenet.transfer.TransferControlProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IOpticalTransferController {

    TransferControl<IOpticalTransferController> CONTROL = new TransferControl<>("Laser") {

        @Override
        public @NotNull IOpticalTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            IOpticalTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull IOpticalTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    IOpticalTransferController DEFAULT = new IOpticalTransferController() {};

    IOpticalTransferController NO_PASSAGE = new IOpticalTransferController() {

        @Override
        public boolean queryHandler(DataQueryObject query, IDataAccess handler) {
            return false;
        }
    };

    /**
     * @return whether the request should be cancelled
     */
    default boolean queryHandler(DataQueryObject query, IDataAccess handler) {
        if (query.traverseTo(handler)) return handler.accessData(query);
        else return false;
    }
}
