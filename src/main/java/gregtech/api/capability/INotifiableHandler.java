package gregtech.api.capability;

import gregtech.api.metatileentity.MetaTileEntity;

import java.util.Iterator;
import java.util.LinkedList;

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

    default void notifyMetaTileEntitiesOfChange(boolean isExport) {
        Iterator<MetaTileEntity> iterator = getNotifiableMetaTileEntities().iterator();
        while (iterator.hasNext()) {
            MetaTileEntity metaTileEntity = iterator.next();
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                if (isExport) metaTileEntity.setOutputsDirty(true);
                else metaTileEntity.setInputsDirty(true);
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * @return a List containing the notifiable MetaTileEntities
     */
    LinkedList<MetaTileEntity> getNotifiableMetaTileEntities();

    /**
     * @param metaTileEntity MetaTileEntity to be notified
     */
    default void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        getNotifiableMetaTileEntities().add(metaTileEntity);
    }
}
