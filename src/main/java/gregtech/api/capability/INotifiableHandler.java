package gregtech.api.capability;

import gregtech.api.metatileentity.MetaTileEntity;

import java.util.HashSet;
import java.util.Iterator;

/**
 * For Item and Fluid handlers capable of notifying entities when
 * their contents change
 */
public interface INotifiableHandler {

    /**
     * Notifies the entities of the changes in their inventories
     * An iterator is used to remove invalid TileEntities
     * @param isExport boolean specifying if a handler is an output handler
     */

    default void notifyMetaTileEntitiesOfChange(boolean isExport) {
        Iterator<MetaTileEntity> iterator = getNotifiableMetaTileEntities().iterator();
        while (iterator.hasNext()) {
            MetaTileEntity mte = iterator.next();
            if (mte != null && mte.isValid()) {
                if (isExport) mte.setOutputsDirty(true);
                else mte.setInputsDirty(true);
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * returns a HashSet containing the notifiable MetaTileEntities
     * @return
     */
    HashSet<MetaTileEntity> getNotifiableMetaTileEntities();

    /**
     *
     * @param metaTileEntity MetaTileEntity to be notified
     */
    default void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        getNotifiableMetaTileEntities().add(metaTileEntity);
    }
}
