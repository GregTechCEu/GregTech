package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.ItemStackKey;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;
import java.util.Map;

public class CoverRoboticArm extends CoverConveyor {

    protected TransferMode transferMode;
    protected int itemsTransferBuffered;

    public CoverRoboticArm(ICoverable coverable, EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(coverable, attachedSide, tier, itemsPerSecond);
        this.transferMode = TransferMode.TRANSFER_ANY;
        this.filterHolder.setMaxStackSize(1);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            Textures.ARM_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        } else {
            Textures.ARM_OVERLAY_INVERTED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    protected int doTransferItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        if (conveyorMode == ConveyorMode.EXPORT && itemHandler instanceof ItemNetHandler && transferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        if (conveyorMode == ConveyorMode.IMPORT && myItemHandler instanceof ItemNetHandler && transferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        switch (transferMode) {
            case TRANSFER_ANY:
                return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
            case TRANSFER_EXACT:
                return doTransferExact(itemHandler, myItemHandler, maxTransferAmount);
            case KEEP_EXACT:
                return doKeepExact(itemHandler, myItemHandler, maxTransferAmount);
            default:
                return 0;
        }
    }

    protected int doTransferExact(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<ItemStackKey, TypeItemInfo> sourceItemAmount = doCountSourceInventoryItemsByType(itemHandler, myItemHandler);
        Iterator<ItemStackKey> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStackKey key = iterator.next();
            TypeItemInfo sourceInfo = sourceItemAmount.get(key);
            int itemAmount = sourceInfo.totalCount;
            int itemToMoveAmount = filterHolder.getSlotTransferLimit(sourceInfo.filterSlot);
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
            if (maxTotalTransferAmount >= itemInfo.totalCount) {
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
            int itemToKeepAmount = filterHolder.getSlotTransferLimit(sourceInfo.filterSlot);
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

    public int getBuffer() {
        return itemsTransferBuffered;
    }

    public void buffer(int amount) {
        itemsTransferBuffered += amount;
    }

    public void clearBuffer() {
        itemsTransferBuffered = 0;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        this.coverHolder.markDirty();
        this.filterHolder.setMaxStackSize(transferMode.maxStackSize);
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    @Override
    protected String getUITitle() {
        return "cover.robotic_arm.title";
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 196);
        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new TextWidget(new Text(getUITitle()).localise(GTValues.VN[tier]))
                        .setPos(6, 6))
                .bindPlayerInventory(buildContext.getPlayer())
                .widget(new Column()
                        .widget(new TextWidget(new Text("cover.transfer_rate").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(new Text("cover.mode").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(new Text("cover.mode.manual_io").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(new Text("cover.conveyor.distribution_mode").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(new Text("cover.transfer_mode").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setSize(80, 12))
                        .widget(new TextWidget(new Text("cover.transfer_amount").localise())
                                .setTextAlignment(Alignment.CenterLeft)
                                .setTicker(this::showTransferAmountField)
                                .setSize(80, 12))
                        .setPos(7, 18)
                        .setSize(80, 72))
                .widget(new Column()
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -8, -64, -512, this::adjustTransferRate))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(this::getTransferRate)
                                        .setSetterInt(this::setTransferRate)
                                        .setNumbers(1, maxItemTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 8, 64, 512, this::adjustTransferRate))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12)))
                        .widget(new CycleButtonWidget()
                                .setForEnum(ConveyorMode.class, this::getConveyorMode, this::setConveyorMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(ConveyorMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(ManualImportExportMode.class))
                                .addTooltip(0, Text.localised(ManualImportExportMode.values()[0].localeTooltip))
                                .addTooltip(1, Text.localised(ManualImportExportMode.values()[1].localeTooltip))
                                .addTooltip(2, Text.localised(ManualImportExportMode.values()[2].localeTooltip))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(DistributionMode.class, this::getDistributionMode, this::setDistributionMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(DistributionMode.class))
                                .addTooltip(0, Text.localised(DistributionMode.values()[0].localeTooltip))
                                .addTooltip(1, Text.localised(DistributionMode.values()[1].localeTooltip))
                                .addTooltip(2, Text.localised(DistributionMode.values()[2].localeTooltip))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(TransferMode.class, this::getTransferMode, this::setTransferMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(TransferMode.class))
                                .addTooltip(0, Text.localised(TransferMode.values()[0].localeTooltip))
                                .addTooltip(1, Text.localised(TransferMode.values()[1].localeTooltip))
                                .addTooltip(2, Text.localised(TransferMode.values()[2].localeTooltip))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -8, -64, -512, getFilterHolder()::adjustTransferStackSize))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(getFilterHolder()::getTransferStackSize)
                                        .setSetterInt(getFilterHolder()::setTransferStackSize)
                                        .setNumbers(1, maxItemTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .setTextColor(0xFFFFFF)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                        .setSize(56, 12))
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(1, 8, 64, 512, getFilterHolder()::adjustTransferStackSize))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .setTicker(this::showTransferAmountField))
                        .setPos(89, 18)
                        .setSize(80, 72))
                .widget(filterHolder.createFilterUI(buildContext)
                        .setPos(7, 90));
        return builder.build();
    }

    private void showTransferAmountField(Widget widget) {
        boolean show = getFilterHolder().showGlobalTransferLimitSlider() && getTransferMode() != TransferMode.TRANSFER_ANY;
        if (widget.isEnabled() != show) {
            widget.setEnabled(show);
        }
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
