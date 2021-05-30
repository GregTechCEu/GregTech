package gregtech.api.capability;

import gregtech.api.metatileentity.MetaTileEntity;

/**
 * For Item and Fluid handlers capable of notifying entities when
 * their contents change
 */
public interface INotifiableHandler {

    /**
     * Notifies the entities of the changes in their inventories
     * An iterator is used to remove invalid TileEntities
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
