package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.mui.GTGuiTextures;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

public class CoverRoboticArm extends CoverConveyor {

    protected TransferMode transferMode;
    protected boolean noTransferDueToMinimum = false;

    public CoverRoboticArm(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                           @NotNull EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(definition, coverableView, attachedSide, tier, itemsPerSecond);
        this.transferMode = TransferMode.TRANSFER_ANY;
        this.itemFilterContainer.setMaxTransferSize(1);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            Textures.ARM_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        } else {
            Textures.ARM_OVERLAY_INVERTED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(Textures.ARM_OVERLAY).setPlateQuads(tier).build();
    }

    @Override
    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(Textures.ARM_OVERLAY_INVERTED).setPlateQuads(tier).build();
    }

    @Override
    protected void refreshBuffer(int transferRate) {
        if (this.transferMode == TransferMode.TRANSFER_EXACT && noTransferDueToMinimum) {
            ItemFilterContainer filter = this.getItemFilter();
            if (filter != null) {
                this.noTransferDueToMinimum = false;
                this.itemsLeftToTransferLastSecond += transferRate;
                int max = filter.getTransferSize();
                if (this.itemsLeftToTransferLastSecond > max) {
                    this.itemsLeftToTransferLastSecond = max;
                }
                return;
            }
        }
        super.refreshBuffer(transferRate);
    }

    @Override
    protected void performTransferOnUpdate(@NotNull IItemHandler sourceHandler, @NotNull IItemHandler destHandler) {
        if (transferMode != TransferMode.TRANSFER_EXACT) {
            super.performTransferOnUpdate(sourceHandler, destHandler);
            return;
        }
        ItemFilterContainer filter = this.getItemFilter();
        if (filter == null) return;
        IntUnaryOperator reqFlow = s -> {
            int limit = filter.getTransferLimit(s);
            if (getItemsLeftToTransfer() < limit) {
                noTransferDueToMinimum = true;
                return 0;
            } else return limit;
        };
        performTransfer(sourceHandler, destHandler, true, reqFlow, reqFlow, (a, b) -> reportItemsTransfer(b));
    }

    @Override
    protected int simpleInsert(@NotNull IItemHandler handler, ItemTestObject testObject, int count,
                               boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            assert getItemFilter() != null;
            int kept = getItemFilter().getTransferLimit(testObject.recombine());
            count = Math.min(count, kept - computeContained(handler, testObject));
        }
        return super.simpleInsert(handler, testObject, count, simulate);
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.getCoverableView().markDirty();
            this.itemFilterContainer.setMaxTransferSize(transferMode.maxStackSize);
            writeCustomData(GregtechDataCodes.UPDATE_TRANSFER_MODE,
                    buffer -> buffer.writeByte(this.transferMode.ordinal()));
        }
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    private boolean shouldDisplayAmountSlider() {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            return false;
        }
        return itemFilterContainer.showGlobalTransferLimitSlider();
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        return super.buildUI(guiData, guiSyncManager).height(192 + 36 + 18 + 2);
    }

    @Override
    protected ParentWidget<Column> createUI(ModularPanel mainPanel, PanelSyncManager guiSyncManager) {
        EnumSyncValue<TransferMode> transferMode = new EnumSyncValue<>(TransferMode.class, this::getTransferMode,
                this::setTransferMode);
        guiSyncManager.syncValue("transfer_mode", transferMode);

        var filterTransferSize = new StringSyncValue(
                () -> String.valueOf(this.itemFilterContainer.getTransferSize()),
                s -> this.itemFilterContainer.setTransferSize(Integer.parseInt(s)));
        filterTransferSize.updateCacheFromSource(true);

        return super.createUI(mainPanel, guiSyncManager)
                .child(new EnumRowBuilder<>(TransferMode.class)
                        .value(transferMode)
                        .lang("cover.generic.transfer_mode")
                        .overlay(GTGuiTextures.TRANSFER_MODE_OVERLAY)
                        .build())
                .child(new Row().right(0).coverChildrenHeight()
                        .child(new TextFieldWidget().widthRel(0.5f).right(0)
                                .setEnabledIf(w -> shouldDisplayAmountSlider())
                                .setNumbers(0, Integer.MAX_VALUE)
                                .value(filterTransferSize)
                                .setTextColor(Color.WHITE.darker(1))));
    }

    @Override
    protected int getMaxStackSize() {
        return getTransferMode().maxStackSize;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(this.transferMode.ordinal());
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferMode = TransferMode.VALUES[packetBuffer.readByte()];
        this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_TRANSFER_MODE) {
            this.transferMode = TransferMode.VALUES[buf.readByte()];
            this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferMode", transferMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.transferMode = TransferMode.VALUES[tagCompound.getInteger("TransferMode")];
        this.itemFilterContainer.setMaxTransferSize(this.transferMode.maxStackSize);
        super.readFromNBT(tagCompound);
    }

    protected int computeContained(@NotNull IItemHandler handler, @NotNull ItemTestObject testObject) {
        int found = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack contained = handler.getStackInSlot(i);
            if (testObject.test(contained)) {
                found += contained.getCount();
            }
        }
        return found;
    }
}
