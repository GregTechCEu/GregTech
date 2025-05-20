package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;

import appeng.api.storage.data.IAEItemStack;

public class AEItemDisplaySlot extends AEDisplaySlot<IAEItemStack> {

    public AEItemDisplaySlot(ExportOnlyAEItemList itemList, int index) {
        super(itemList.getInventory()[index]);
    }
}
