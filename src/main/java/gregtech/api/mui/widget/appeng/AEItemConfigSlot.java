package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;

import appeng.api.storage.data.IAEItemStack;

public class AEItemConfigSlot extends AEConfigSlot<IAEItemStack> {

    public AEItemConfigSlot(ExportOnlyAEItemList itemList, int index) {
        super(itemList.getInventory()[index], itemList.isStocking());
    }
}
