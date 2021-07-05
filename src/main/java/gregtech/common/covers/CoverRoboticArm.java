package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.render.Textures;
import gregtech.api.util.ItemStackKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CoverRoboticArm extends CoverConveyor {

    protected TransferMode transferMode;
    protected int itemsTransferBuffered;

    public CoverRoboticArm(ICoverable coverable, EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(coverable, attachedSide, tier, itemsPerSecond);
        this.transferMode = TransferMode.TRANSFER_ANY;
        this.itemFilterContainer.setMaxStackSize(1);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ARM_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    protected int doTransferItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        switch (transferMode) {
            case TRANSFER_ANY: return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
            case TRANSFER_EXACT: return doTransferExact(itemHandler, myItemHandler, maxTransferAmount);
            case KEEP_EXACT: return doKeepExact(itemHandler, myItemHandler, maxTransferAmount);
            default: return 0;
        }
    }

    protected int doTransferExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<ItemStackKey, TypeItemInfo> sourceItemAmount = doCountSourceInventoryItemsByType(itemHandler, myItemHandler);
        Iterator<ItemStackKey> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStackKey key = iterator.next();
            TypeItemInfo sourceInfo = sourceItemAmount.get(key);
            int itemAmount = sourceInfo.totalCount;
            Set<ItemStackKey> matchedItems = Collections.singleton(key);
            int itemToMoveAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot, matchedItems);
            if (itemAmount >= itemToMoveAmount) {
                sourceInfo.totalCount = itemToMoveAmount;
            } else {
                iterator.remove();
            }
        }

        int itemsTransferred = 0;
        int maxTotalTransferAmount = maxTransferAmount + itemsTransferBuffered;
        boolean notEnoughTransferRate = false;
        for (TypeItemInfo itemInfo : sourceItemAmount.values()) {
            if(maxTotalTransferAmount >= itemInfo.totalCount) {
                boolean result = doTransferItemsExact(itemHandler, myItemHandler, itemInfo);
                itemsTransferred += result ? itemInfo.totalCount : 0;
                maxTotalTransferAmount -= result ? itemInfo.totalCount : 0;
            } else {
                notEnoughTransferRate = true;
            }
        }
        //if we didn't transfer anything because of too small transfer rate, buffer it
        if (itemsTransferred == 0 && notEnoughTransferRate) {
            itemsTransferBuffered += maxTransferAmount;
        } else {
            //otherwise, if transfer succeed, empty transfer buffer value
            itemsTransferBuffered = 0;
        }
        return Math.min(itemsTransferred, maxTransferAmount);
    }

    protected int doKeepExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<Object, GroupItemInfo> currentItemAmount = doCountDestinationInventoryItemsByMatchIndex(itemHandler, myItemHandler);
        Map<Object, GroupItemInfo> sourceItemAmounts = doCountDestinationInventoryItemsByMatchIndex(myItemHandler, itemHandler);
        Iterator<Object> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            Object filterSlotIndex = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filterSlotIndex);
            int itemToKeepAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot, sourceInfo.itemStackTypes);
            int itemAmount = 0;
            if (currentItemAmount.containsKey(filterSlotIndex)) {
                GroupItemInfo destItemInfo = currentItemAmount.get(filterSlotIndex);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }
        return doTransferItemsByGroup(itemHandler, myItemHandler, sourceItemAmounts, maxTransferAmount);
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        this.coverHolder.markDirty();
        this.itemFilterContainer.setMaxStackSize(transferMode.maxStackSize);
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    @Override
    protected String getUITitle() {
        return "cover.robotic_arm.title";
    }

    @Override
    protected ModularUI buildUI(Builder builder, EntityPlayer player) {
        WidgetGroup filterGroup = new WidgetGroup();
        filterGroup.addWidget(new CycleButtonWidget(91, 45, 75, 20,
            TransferMode.class, this::getTransferMode, this::setTransferMode)
            .setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));

        return super.buildUI(builder.widget(filterGroup), player);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferMode = TransferMode.values()[tagCompound.getInteger("TransferMode")];
    }

}
