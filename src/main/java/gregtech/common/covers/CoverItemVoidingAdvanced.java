package gregtech.common.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CoverItemVoidingAdvanced extends CoverItemVoiding {

    protected VoidingMode voidingMode;

    public CoverItemVoidingAdvanced(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                    @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
        this.voidingMode = VoidingMode.VOID_ANY;
        this.itemFilterContainer.setMaxTransferSize(1);
    }

    @Override
    protected void doTransferItems() {
        IItemHandler myItemHandler = getCoverableView().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                getAttachedSide());
        if (myItemHandler == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(myItemHandler);
            case VOID_OVERFLOW -> voidOverflow(myItemHandler);
        }
    }

    protected void voidOverflow(IItemHandler myItemHandler) {
        Map<ItemStack, TypeItemInfo> itemTypeCount = countInventoryItemsByType(myItemHandler);
        for (TypeItemInfo typeItemInfo : itemTypeCount.values()) {

            int itemToVoidAmount = 0;
            if (!getItemFilterContainer().hasFilter()) {
                itemToVoidAmount = typeItemInfo.totalCount - itemFilterContainer.getTransferSize();
            } else {
                var result = itemFilterContainer.match(typeItemInfo.itemStack);
                itemToVoidAmount = result.getMatchedStack().getCount();
            }

            if (itemToVoidAmount <= 0) {
                continue;
            }

            for (int srcIndex = 0; srcIndex < myItemHandler.getSlots(); srcIndex++) {
                ItemStack is = myItemHandler.getStackInSlot(srcIndex);
                if (!is.isEmpty() && ItemStack.areItemsEqual(is, typeItemInfo.itemStack) &&
                        ItemStack.areItemStackTagsEqual(is, typeItemInfo.itemStack)) {
                    ItemStack extracted = myItemHandler.extractItem(srcIndex, itemToVoidAmount, false);
                    if (!extracted.isEmpty()) {
                        itemToVoidAmount -= extracted.getCount();
                    }
                }
                if (itemToVoidAmount == 0) {
                    break;
                }
            }
        }
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        return super.buildUI(guiData, guiSyncManager).height(192 + 18);
    }

    @Override
    protected ParentWidget<Column> createUI(ModularPanel mainPanel, GuiSyncManager guiSyncManager) {
        var voidingMode = new EnumSyncValue<>(VoidingMode.class, this::getVoidingMode, this::setVoidingMode);
        guiSyncManager.syncValue("voiding_mode", voidingMode);

        var filterTransferSize = new StringSyncValue(
                () -> String.valueOf(this.itemFilterContainer.getTransferSize()),
                s -> this.itemFilterContainer.setTransferSize(Integer.parseInt(s)));
        filterTransferSize.updateCacheFromSource(true);
        var transferTextField = new TextFieldWidget().widthRel(0.5f).right(0);
        transferTextField.setEnabled(this.itemFilterContainer.showGlobalTransferLimitSlider() &&
                this.voidingMode == VoidingMode.VOID_OVERFLOW);

        return super.createUI(mainPanel, guiSyncManager)
                .child(new EnumRowBuilder<>(VoidingMode.class)
                        .value(voidingMode)
                        .lang("cover.voiding.voiding_mode")
                        // .overlay(GTGuiTextures.TRANSFER_MODE_OVERLAY) todo voiding mode overlay
                        .build())
                .child(new Row().right(0).coverChildrenHeight()
                        .child(transferTextField
                                .setEnabledIf(w -> this.itemFilterContainer.showGlobalTransferLimitSlider() &&
                                        this.voidingMode == VoidingMode.VOID_OVERFLOW)
                                .setNumbers(0, Integer.MAX_VALUE)
                                .value(filterTransferSize)
                                .setTextColor(Color.WHITE.darker(1))));
    }

    @Override
    protected int getMaxStackSize() {
        return getVoidingMode().maxStackSize;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ITEM_VOIDING_ADVANCED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;
        this.itemFilterContainer.setMaxTransferSize(getMaxStackSize());
        this.getCoverableView().markDirty();
    }

    public VoidingMode getVoidingMode() {
        return voidingMode;
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.voidingMode = packetBuffer.readEnumValue(VoidingMode.class);
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeEnumValue(voidingMode);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("VoidMode", voidingMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.voidingMode = VoidingMode.values()[tagCompound.getInteger("VoidMode")];
    }
}
