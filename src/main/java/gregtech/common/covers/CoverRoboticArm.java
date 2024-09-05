package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.pipelike.net.item.IItemTransferController;
import gregtech.common.pipelike.net.item.ItemEQTraverseData;
import gregtech.common.pipelike.net.item.ItemRRTraverseData;
import gregtech.common.pipelike.net.item.ItemTraverseData;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
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
        if (transferMode == TransferMode.TRANSFER_ANY) {
            super.performTransferOnUpdate(sourceHandler, destHandler);
            return;
        }
        ItemFilterContainer filter = this.getItemFilter();
        if (filter == null) return;
        if (transferMode == TransferMode.KEEP_EXACT) {
            IntUnaryOperator maxflow = s -> Math.min(filter.getTransferLimit(s), getItemsLeftToTransfer());
            reportItemsTransfer(performTransfer(sourceHandler, destHandler, true, s -> 0, maxflow, null));
        } else if (transferMode == TransferMode.TRANSFER_EXACT) {
            IntUnaryOperator maxflow = s -> {
                int limit = filter.getTransferLimit(s);
                if (getItemsLeftToTransfer() < limit) {
                    noTransferDueToMinimum = true;
                    return 0;
                } else return limit;
            };
            performTransfer(sourceHandler, destHandler, true, maxflow, maxflow, (a, b) -> reportItemsTransfer(b));
        }
    }

    @Override
    protected @NotNull ItemTraverseData getTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                              long queryTick, BlockPos sourcePos, EnumFacing inputFacing) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull ItemEQTraverseData getEQTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, EnumFacing inputFacing) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemEQTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getEQTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull ItemRRTraverseData getRRTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, EnumFacing inputFacing,
                                                  boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemRRTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing,
                    getRoundRobinCache(simulate));
        }
        return super.getRRTD(net, testObject, simulator, queryTick, sourcePos, inputFacing, simulate);
    }

    @Override
    protected int simpleInsert(@NotNull IItemHandler destHandler, ItemTestObject testObject, int count,
                               boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            assert getItemFilter() != null;
            int kept = getItemFilter().getTransferLimit(testObject.recombine());
            count = Math.min(count, kept - computeContained(destHandler, testObject));
        }
        return super.simpleInsert(destHandler, testObject, count, simulate);
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
    public int insertToHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemHandler destHandler,
                               boolean simulate) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            if (transferMode == TransferMode.KEEP_EXACT) {
                int contained = computeContained(destHandler, testObject);
                assert getItemFilter() != null;
                int keep = getItemFilter().getTransferLimit(testObject.recombine());
                if (contained >= keep) return amount;
                int allowed = Math.min(keep - contained, amount);
                return (amount - allowed) + super.insertToHandler(testObject, allowed, destHandler, simulate);
            } else if (transferMode == TransferMode.TRANSFER_EXACT) {
                assert getItemFilter() != null;
                int required = getItemFilter().getTransferLimit(testObject.recombine());
                if (amount < required) return amount;
                return (amount - required) + super.insertToHandler(testObject, required, destHandler, simulate);
            }
        }
        return super.insertToHandler(testObject, amount, destHandler, simulate);
    }

    @Override
    public int extractFromHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemHandler sourceHandler,
                                  boolean simulate) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            // TODO should extraction instead be ignored for transfer exact?
            if (transferMode == TransferMode.TRANSFER_EXACT) {
                assert getItemFilter() != null;
                int required = getItemFilter().getTransferLimit(testObject.recombine());
                if (amount < required) return 0;
                else amount = required;
            }
        }
        return super.extractFromHandler(testObject, amount, sourceHandler, simulate);
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

    protected class KeepItemTraverseData extends ItemTraverseData {

        public KeepItemTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                    BlockPos sourcePos, EnumFacing inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler container = capability.getValue()
                        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                capability.getKey().getOpposite());
                if (container != null) {
                    int contained = computeContained(container, getTestObject());
                    assert getItemFilter() != null;
                    int kept = getItemFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow = IItemTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, availableFlow), container, simulating());
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    protected class KeepItemEQTraverseData extends ItemEQTraverseData {

        public KeepItemEQTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                      BlockPos sourcePos, EnumFacing inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        protected void compute(@NotNull WorldPipeNetNode destination) {
            this.destCount = 0;
            this.maxMinFlow = 0;
            for (var capability : destination.getTileEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler container = capability.getValue()
                        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                capability.getKey().getOpposite());
                if (container != null) {
                    int contained = computeContained(container, getTestObject());
                    assert getItemFilter() != null;
                    int kept = getItemFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    if (destCount == 0) maxMinFlow = Integer.MAX_VALUE;
                    destCount += 1;
                    int test = kept - contained;
                    maxMinFlow = Math.min(maxMinFlow, test -
                            IItemTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                                    .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(), test,
                                            container, true));
                }
            }
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode node, long flowReachingNode,
                                          int expectedDestinations) {
            long availableFlow = flowReachingNode;
            long flowPerDestination = flowReachingNode / expectedDestinations;
            if (flowPerDestination == 0) return 0;
            for (var capability : node.getTileEntity().getTargetsWithCapabilities(node).entrySet()) {
                if (GTUtility.arePosEqual(node.getEquivalencyData(), sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler container = capability.getValue()
                        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                capability.getKey().getOpposite());
                if (container != null) {
                    int contained = computeContained(container, getTestObject());
                    assert getItemFilter() != null;
                    int kept = getItemFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow = IItemTransferController.CONTROL.get(node.getTileEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, flowPerDestination), container, simulating());
                }
            }
            return flowReachingNode - availableFlow;
        }
    }

    protected class KeepItemRRTraverseData extends ItemRRTraverseData {

        public KeepItemRRTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                      BlockPos sourcePos, EnumFacing inputFacing,
                                      @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing, cache);
        }

        @Override
        public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IItemHandler> data,
                                          @NotNull WorldPipeNetNode destination,
                                          long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            EnumFacing pointerFacing = data.getPointerFacing(getSimulatorKey());
            if (GTUtility.arePosEqual(destination.getEquivalencyData(), sourcePos) && pointerFacing == inputFacing)
                return 0; // anti insert-to-our-source logic

            IItemHandler container = data.getAtPointer(destination, getSimulatorKey());
            if (container != null) {
                int contained = computeContained(container, getTestObject());
                assert getItemFilter() != null;
                int kept = getItemFilter().getTransferLimit(getTestObject().recombine());
                if (contained >= kept) return 0;
                availableFlow = IItemTransferController.CONTROL.get(destination.getTileEntity().getCoverHolder()
                        .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                                (int) Math.min(kept - contained, availableFlow), container, simulating());
            }
            return flowReachingDestination - availableFlow;
        }
    }
}
