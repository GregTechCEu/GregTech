package gregtech.api.capability;

import gregtech.api.metatileentity.MetaTileEntity;

/**
 * For Item and Fluid handlers capable of notifying entities when
 * their contents change
 */
public interface INotifiableHandler {

    /**
     * Adds the notified handler to the notified list
     *
     * @param isExport boolean specifying if a handler is an output handler
     */

    default <T> void addToNotifiedList(MetaTileEntity metaTileEntity, T handler, boolean isExport) {
        if (metaTileEntity != null && metaTileEntity.isValid()) {
            if (isExport) {
                metaTileEntity.addNotifiedOutput(handler);
            } else {
                metaTileEntity.addNotifiedInput(handler);
            }
        }
    }

    /**
     * @param metaTileEntity MetaTileEntity to be notified
     */
    default void setNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {

    }
}
