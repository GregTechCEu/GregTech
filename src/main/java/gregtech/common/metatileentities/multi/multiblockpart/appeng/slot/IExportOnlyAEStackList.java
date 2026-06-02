package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import appeng.api.storage.data.IAEStack;
import org.jetbrains.annotations.NotNull;

public interface IExportOnlyAEStackList<AEStackType extends IAEStack<AEStackType>> {

    @NotNull
    ExportOnlyAESlot<AEStackType> @NotNull [] getInventory();

    boolean isAutoPull();

    boolean isStocking();
}
