package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.filter.CoverWithItemFilter;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.traverse.RoundRobinCache;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.EQTraverse;
import gregtech.api.graphnet.traverse.FDTraverse;
import gregtech.api.graphnet.traverse.RRTraverse;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.graphnet.traverse.iter.EdgeSelector;
import gregtech.api.graphnet.traverse.iter.NetClosestIterator;
import gregtech.api.graphnet.traverse.iter.NetIterator;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.function.BiIntConsumer;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.covers.filter.MatchResult;
import gregtech.common.covers.filter.MergabilityInfo;
import gregtech.common.pipelike.net.item.ItemCapabilityObject;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class CoverConveyor extends CoverBase implements CoverWithUI, ITickable, IControllable, CoverWithItemFilter {

    public final int tier;
    public final int maxItemTransferRate;
    private int transferRate;
    protected ConveyorMode conveyorMode;
    protected DistributionMode distributionMode;
    protected boolean transferByFilterGroups;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected final ItemFilterContainer itemFilterContainer;
    protected int itemsLeftToTransferLastSecond;
    private CoverableItemHandlerWrapper itemHandlerWrapper;
    protected boolean isWorkingAllowed = true;

    protected final RoundRobinCache roundRobinCache = RoundRobinCache.create();

    protected @Nullable CoverRenderer rendererInverted;

    public CoverConveyor(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                         @NotNull EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(definition, coverableView, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = itemsPerSecond;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.conveyorMode = ConveyorMode.EXPORT;
        this.distributionMode = DistributionMode.FLOOD;
        this.transferByFilterGroups = false;
        this.itemFilterContainer = new ItemFilterContainer(this);
    }

    @Override
    public @Nullable ItemFilterContainer getItemFilter() {
        return itemFilterContainer;
    }

    @Override
    public ItemFilterMode getFilterMode() {
        return ItemFilterMode.FILTER_BOTH;
    }

    @Override
    public ManualImportExportMode getManualMode() {
        return this.manualImportExportMode;
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = MathHelper.clamp(transferRate, 1, maxItemTransferRate);
        CoverableView coverable = getCoverableView();
        coverable.markDirty();
    }

    public int getTransferRate() {
        return transferRate;
    }

    protected void adjustTransferRate(int amount) {
        setTransferRate(MathHelper.clamp(transferRate + amount, 1, maxItemTransferRate));
    }

    public void setConveyorMode(ConveyorMode conveyorMode) {
        this.conveyorMode = conveyorMode;
        writeCustomData(GregtechDataCodes.UPDATE_COVER_MODE, buf -> buf.writeEnumValue(conveyorMode));
        markDirty();
    }

    public ConveyorMode getConveyorMode() {
        return conveyorMode;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
        this.roundRobinCache.clear();
        markDirty();
    }

    public ManualImportExportMode getManualImportExportMode() {
        return manualImportExportMode;
    }

    protected void setManualImportExportMode(ManualImportExportMode manualImportExportMode) {
        this.manualImportExportMode = manualImportExportMode;
        markDirty();
    }

    public ItemFilterContainer getItemFilterContainer() {
        return itemFilterContainer;
    }

    @Override
    public void update() {
        CoverableView coverable = getCoverableView();
        long timer = coverable.getOffsetTimer();
        if (timer % 5 == 0 && isWorkingAllowed && getItemsLeftToTransfer() > 0) {
            EnumFacing side = getAttachedSide();
            TileEntity tileEntity = coverable.getNeighbor(side);
            IItemHandler itemHandler = tileEntity == null ? null :
                    tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            IItemHandler myItemHandler = coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (itemHandler != null && myItemHandler != null) {
                if (conveyorMode == ConveyorMode.EXPORT) {
                    performTransferOnUpdate(myItemHandler, itemHandler);
                } else {
                    performTransferOnUpdate(itemHandler, myItemHandler);
                }
            }
        }
        if (timer % 20 == 0) {
            refreshBuffer(transferRate);
        }
    }

    protected int getItemsLeftToTransfer() {
        return itemsLeftToTransferLastSecond;
    }

    protected void reportItemsTransfer(int transferred) {
        this.itemsLeftToTransferLastSecond -= transferred;
    }

    protected void refreshBuffer(int transferRate) {
        this.itemsLeftToTransferLastSecond = transferRate;
    }

    protected void performTransferOnUpdate(@NotNull IItemHandler sourceHandler, @NotNull IItemHandler destHandler) {
        reportItemsTransfer(performTransfer(sourceHandler, destHandler, false, i -> 0,
                i -> getItemsLeftToTransfer(), null));
    }

    /**
     * Performs transfer
     *
     * @param sourceHandler  the handler to pull from
     * @param destHandler    the handler to push to
     * @param byFilterSlot   whether to perform the transfer by filter slot.
     * @param minTransfer    the minimum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param maxTransfer    the maximum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param transferReport where transfer is reported; a is the filter slot, b is the amount of transfer.
     *                       Each filter slot will report its transfer before the next slot is calculated.
     * @return how much was transferred in total.
     */
    protected int performTransfer(@NotNull IItemHandler sourceHandler, @NotNull IItemHandler destHandler,
                                  boolean byFilterSlot, @NotNull IntUnaryOperator minTransfer,
                                  @NotNull IntUnaryOperator maxTransfer, @Nullable BiIntConsumer transferReport) {
        // if the source handler or the dest handler is a pipe, perform a pipenet transfer. Otherwise, do a simple one.
        if (sourceHandler instanceof ItemCapabilityObject s) {
            if (destHandler instanceof ItemCapabilityObject d) {
                return transferWithinNet(s.getNode(), d.getNode(), byFilterSlot, minTransfer, maxTransfer,
                        transferReport);
            } else {
                PipeCapabilityWrapper wrapper = s.getNode().getTileEntity().getWrapperForNode(s.getNode());
                if (wrapper instanceof NodeManagingPCW managingPCW) {
                    // if export, we know we're on the pipe; otherwise, we're on the tile.
                    EnumFacing facing = conveyorMode == ConveyorMode.EXPORT ? getAttachedSide() :
                            getAttachedSide().getOpposite();
                    NetNode node = managingPCW.getNodeForFacing(facing);
                    if (node == null) return 0;
                    return transferWithinNet(s.getNode(), node, byFilterSlot, minTransfer, maxTransfer, transferReport);
                }
                return 0;
            }
        } else {
            if (destHandler instanceof ItemCapabilityObject d) {
                PipeCapabilityWrapper wrapper = d.getNode().getTileEntity().getWrapperForNode(d.getNode());
                if (wrapper instanceof NodeManagingPCW managingPCW) {
                    // if import, we know we're on the pipe; otherwise, we're on the tile.
                    EnumFacing facing = conveyorMode == ConveyorMode.IMPORT ? getAttachedSide() :
                            getAttachedSide().getOpposite();
                    NetNode node = managingPCW.getNodeForFacing(facing);
                    if (node == null) return 0;
                    return transferWithinNet(node, d.getNode(), byFilterSlot, minTransfer, maxTransfer, transferReport);
                }
                return 0;
            } else {
                return simpleTransfer(sourceHandler, destHandler, byFilterSlot, minTransfer, maxTransfer,
                        transferReport);
            }
        }
    }

    protected RoundRobinCache getRoundRobinCache(boolean simulate) {
        return simulate ? roundRobinCache.copy() : roundRobinCache;
    }

    /**
     * Performs transfer between two nodes of a pipenet.
     *
     * @param sourceNode     the node to pull from
     * @param destNode       the node to push to
     * @param byFilterSlot   whether to perform the transfer by filter slot.
     * @param minTransfer    the minimum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param maxTransfer    the maximum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param transferReport where transfer is reported; a is the filter slot, b is the amount of transfer.
     *                       Each filter slot will report its transfer before the next slot is calculated.
     * @return how much was transferred in total.
     */
    protected int transferWithinNet(@NotNull NetNode sourceNode, @NotNull NetNode destNode,
                                    boolean byFilterSlot, @NotNull IntUnaryOperator minTransfer,
                                    @NotNull IntUnaryOperator maxTransfer, @Nullable BiIntConsumer transferReport) {
        // first, evaluate whether we're the only bridge between two areas in a group
        NetEdge bridge = sourceNode.getNet().getEdge(sourceNode, destNode);
        GraphEdge b1 = GraphEdge.unwrap(bridge);
        GraphEdge b2 = GraphEdge.unwrap(sourceNode.getNet().getEdge(destNode, sourceNode));
        Predicate<Object> predicate = g -> g != b1 && g != b2;
        NetClosestIterator sourceFrontier = new NetClosestIterator(sourceNode,
                EdgeSelector.filtered(EdgeDirection.INCOMING, predicate));
        NetClosestIterator destFrontier = new NetClosestIterator(destNode,
                EdgeSelector.filtered(EdgeDirection.OUTGOING, predicate));
        Map<NetNode, IItemHandler> sourceCandidates = new Object2ObjectOpenHashMap<>();
        Map<NetNode, IItemHandler> destinationCandidates = new Object2ObjectOpenHashMap<>();
        // note that if we checked all edges instead of only incoming for one and outgoing for the other,
        // we would be able to know that we're the only bridge once either frontier exhausted, not both.
        while (sourceFrontier.hasNext() || destFrontier.hasNext()) {
            if (sourceFrontier.hasNext()) {
                NetNode next = sourceFrontier.next();
                // the dest frontier has seen the next node in the source frontier, we are not the only bridge.
                if (destFrontier.getSpanningTreeEdge(next) != null) return 0;
                if (next instanceof NodeExposingCapabilities cap) {
                    IItemHandler handler = cap.getProvider()
                            .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, cap.exposedFacing());
                    if (handler != null && !(handler instanceof ItemCapabilityObject))
                        sourceCandidates.put(next, handler);
                }
            }
            if (destFrontier.hasNext()) {
                NetNode next = destFrontier.next();
                // the source frontier has seen the next node in the dest frontier, we are not the only bridge.
                if (sourceFrontier.getSpanningTreeEdge(next) != null) return 0;
                if (next instanceof NodeExposingCapabilities cap) {
                    IItemHandler handler = cap.getProvider()
                            .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, cap.exposedFacing());
                    if (handler != null && !(handler instanceof ItemCapabilityObject))
                        destinationCandidates.put(next, handler);
                }
            }
        }
        // if we reach this point, we know we are the only bridge and can commence traverse.
        ItemFilterContainer filter = this.getItemFilter();
        byFilterSlot = byFilterSlot && filter != null; // can't be by filter slot if there is no filter
        Int2IntArrayMap extractableByFilterSlot = new Int2IntArrayMap();
        Int2ObjectArrayMap<MergabilityInfo<ItemTestObject>> filterSlotToMergability = new Int2ObjectArrayMap<>();
        ItemHandlerList handlerSourceCandidates = new ItemHandlerList(sourceCandidates.values());
        for (int i = 0; i < handlerSourceCandidates.getSlots(); i++) {
            ItemStack stack = handlerSourceCandidates.extractItem(i, Integer.MAX_VALUE, true);
            int extracted = stack.getCount();
            if (extracted == 0) continue;
            MatchResult match = null;
            if (filter == null || (match = filter.match(stack)).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    filterSlot = match.getFilterIndex();
                }
                extractableByFilterSlot.merge(filterSlot, extracted, Integer::sum);
                final int handlerSlot = i;
                filterSlotToMergability.compute(filterSlot, (k, v) -> {
                    if (v == null) v = new MergabilityInfo<>();
                    v.add(handlerSlot, new ItemTestObject(stack), extracted);
                    return v;
                });
            }
        }
        var iter = extractableByFilterSlot.int2IntEntrySet().fastIterator();
        int totalTransfer = 0;

        while (iter.hasNext()) {
            var next = iter.next();
            int filterSlot = next.getIntKey();
            int min = minTransfer.applyAsInt(filterSlot);
            int max = maxTransfer.applyAsInt(filterSlot);
            if (max < min || max <= 0) continue;
            if (next.getIntValue() >= min) {
                MergabilityInfo<ItemTestObject> mergabilityInfo = filterSlotToMergability.get(filterSlot);
                MergabilityInfo<ItemTestObject>.Merge merge = mergabilityInfo.getLargestMerge();
                // since we can't guarantee the transferability of multiple stack types while just simulating,
                // if the largest merge is not large enough we have to give up.
                if (merge.getCount() >= min) {
                    int slotTransfer = 0;
                    int transfer = Math.min(merge.getCount(), max);
                    // only simulate to test min if necessary
                    if (min > 0) {
                        transfer = attemptNetTransfer(sourceNode.getNet(), bridge, transfer, merge.getTestObject(),
                                sourceFrontier, sourceCandidates, destFrontier, destinationCandidates,
                                SimulatorKey.getNewSimulatorInstance());
                        if (transfer < min) continue;
                    }
                    transfer = attemptNetTransfer(sourceNode.getNet(), bridge, transfer, merge.getTestObject(),
                            sourceFrontier, sourceCandidates, destFrontier, destinationCandidates, null);
                    int remaining = max - transfer;
                    slotTransfer += transfer;
                    if (remaining <= 0) continue;
                    for (MergabilityInfo<ItemTestObject>.Merge otherMerge : mergabilityInfo
                            .getNonLargestMerges(merge)) {
                        transfer = Math.min(otherMerge.getCount(), remaining);
                        // we don't have to simulate here since we no longer need to respect the min
                        transfer = attemptNetTransfer(sourceNode.getNet(), bridge, transfer, merge.getTestObject(),
                                sourceFrontier, sourceCandidates, destFrontier, destinationCandidates, null);
                        remaining -= transfer;
                        slotTransfer += transfer;
                        if (remaining <= 0) break;
                    }
                    if (transferReport != null) transferReport.accept(filterSlot, slotTransfer);
                    totalTransfer += slotTransfer;
                }
            }
        }
        return totalTransfer;
    }

    protected int attemptNetTransfer(IGraphNet net, NetEdge bridge, int limit, ItemTestObject testObject,
                                     NetIterator sources, Map<NetNode, IItemHandler> sourceCandidates,
                                     NetIterator targets, Map<NetNode, IItemHandler> destCandidates,
                                     @Nullable SimulatorKey key) {
        return switch (distributionMode) {
            case FLOOD -> FDTraverse.flood(net,
                    (n, f) -> {
                        if (key == null) ItemCapabilityObject.reportFlow(n, f, testObject);
                    },
                    (e, f) -> ItemCapabilityObject.reportFlow(e, f, testObject, key, true),
                    e -> e == bridge ? limit : e instanceof AbstractNetFlowEdge n ?
                            GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, net, GTUtility.getTick(), key)) : 0,
                    n -> getSupply(n, testObject, sources.getSpanningTreeEdge(n) != null), null, null);
            case EQUALIZED -> EQTraverse.equalDistribution(net,
                    (n, f) -> {
                        if (key == null) ItemCapabilityObject.reportFlow(n, f, testObject);
                    },
                    (e, f) -> ItemCapabilityObject.reportFlow(e, f, testObject, key, true),
                    e -> e == bridge ? limit : e instanceof AbstractNetFlowEdge n ?
                            GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, net, GTUtility.getTick(), key)) : 0,
                    n -> getSupply(n, testObject, sources.getSpanningTreeEdge(n) != null), null, null);
            case ROUND_ROBIN -> {
                roundRobinCache.refresh(sources, targets);
                yield RRTraverse.roundRobin(net, getRoundRobinCache(key != null)
                        .buildSupplier(sourceCandidates.keySet(), destCandidates.keySet()),
                        (n, f) -> {
                            if (key == null) ItemCapabilityObject.reportFlow(n, f, testObject);
                        },
                        (e, f) -> ItemCapabilityObject.reportFlow(e, f, testObject, key, true),
                        e -> e == bridge ? limit : e instanceof AbstractNetFlowEdge n ?
                                GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, net, GTUtility.getTick(), key)) :
                                0,
                        n -> getSupply(n, testObject, sources.getSpanningTreeEdge(n) != null),
                        null, null);
            }
        };
    }

    protected int getSupply(NetNode node, ItemTestObject testObject, boolean supply) {
        return ItemCapabilityObject.getSupply(node, testObject, supply);
    }

    /**
     * Performs transfer without involving the pipenet
     *
     * @param sourceHandler  the handler to pull from
     * @param destHandler    the handler to push to
     * @param byFilterSlot   whether to perform the transfer by filter slot.
     * @param minTransfer    the minimum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param maxTransfer    the maximum allowed transfer amount, when given a filter slot. If no filter exists or not
     *                       transferring by slot, a filter slot of -1 will be passed in.
     * @param transferReport where transfer is reported; a is the filter slot, b is the amount of transfer.
     *                       Each filter slot will report its transfer before the next slot is calculated.
     * @return how much was transferred in total.
     */
    protected int simpleTransfer(@NotNull IItemHandler sourceHandler, @NotNull IItemHandler destHandler,
                                 boolean byFilterSlot, @NotNull IntUnaryOperator minTransfer,
                                 @NotNull IntUnaryOperator maxTransfer, @Nullable BiIntConsumer transferReport) {
        ItemFilterContainer filter = this.getItemFilter();
        byFilterSlot = byFilterSlot && filter != null; // can't be by filter slot if there is no filter
        Int2IntArrayMap extractableByFilterSlot = new Int2IntArrayMap();
        Int2ObjectArrayMap<MergabilityInfo<ItemTestObject>> filterSlotToMergability = new Int2ObjectArrayMap<>();
        for (int i = 0; i < sourceHandler.getSlots(); i++) {
            ItemStack stack = sourceHandler.extractItem(i, Integer.MAX_VALUE, true);
            int extracted = stack.getCount();
            if (extracted == 0) continue;
            MatchResult match = null;
            if (filter == null || (match = filter.match(stack)).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    filterSlot = match.getFilterIndex();
                }
                extractableByFilterSlot.merge(filterSlot, extracted, Integer::sum);
                final int handlerSlot = i;
                filterSlotToMergability.compute(filterSlot, (k, v) -> {
                    if (v == null) v = new MergabilityInfo<>();
                    v.add(handlerSlot, new ItemTestObject(stack), extracted);
                    return v;
                });
            }
        }
        var iter = extractableByFilterSlot.int2IntEntrySet().fastIterator();
        int totalTransfer = 0;
        while (iter.hasNext()) {
            var next = iter.next();
            int filterSlot = next.getIntKey();
            int min = minTransfer.applyAsInt(filterSlot);
            int max = maxTransfer.applyAsInt(filterSlot);
            if (max < min || max <= 0) continue;
            int slotTransfer = 0;
            if (next.getIntValue() >= min) {
                MergabilityInfo<ItemTestObject> mergabilityInfo = filterSlotToMergability.get(filterSlot);
                MergabilityInfo<ItemTestObject>.Merge merge = mergabilityInfo.getLargestMerge();
                // since we can't guarantee the transferability of multiple stack types while just simulating,
                // if the largest merge is not large enough we have to give up.
                if (merge.getCount() >= min) {
                    int transfer = Math.min(merge.getCount(), max);
                    transfer = simpleInsert(destHandler, merge.getTestObject(), transfer, true);
                    if (transfer < min) continue;
                    int toExtract = transfer;
                    for (int handlerSlot : merge.getHandlerSlots()) {
                        toExtract -= sourceHandler.extractItem(handlerSlot, toExtract, false).getCount();
                        if (toExtract == 0) break;
                    }
                    simpleInsert(destHandler, merge.getTestObject(), transfer - toExtract, false);
                    int remaining = max - transfer + toExtract;
                    slotTransfer += transfer;
                    if (remaining <= 0) continue;
                    for (MergabilityInfo<ItemTestObject>.Merge otherMerge : mergabilityInfo
                            .getNonLargestMerges(merge)) {
                        transfer = Math.min(otherMerge.getCount(), remaining);
                        transfer = simpleInsert(destHandler, merge.getTestObject(), transfer, true);
                        toExtract = transfer;
                        for (int handlerSlot : otherMerge.getHandlerSlots()) {
                            toExtract -= sourceHandler.extractItem(handlerSlot, toExtract, false).getCount();
                            if (toExtract == 0) break;
                        }
                        simpleInsert(destHandler, otherMerge.getTestObject(), transfer - toExtract, false);
                        remaining -= transfer;
                        slotTransfer += transfer;
                        if (remaining <= 0) break;
                    }
                }
            }
            if (transferReport != null) transferReport.accept(filterSlot, slotTransfer);
            totalTransfer += slotTransfer;
        }
        return totalTransfer;
    }

    protected int simpleInsert(@NotNull IItemHandler destHandler, ItemTestObject testObject, int count,
                               boolean simulate) {
        int available = count;
        for (int i = 0; i < destHandler.getSlots(); i++) {
            ItemStack toInsert = testObject.recombine(Math.min(available, destHandler.getSlotLimit(i)));
            available -= toInsert.getCount() - destHandler.insertItem(i, toInsert, simulate).getCount();
            if (available == 0) return count;
        }
        return count - available;
    }

    protected static class TypeItemInfo {

        public final ItemStack itemStack;
        public final int filterSlot;
        public final IntList slots;
        public int totalCount;

        public TypeItemInfo(ItemStack itemStack, int filterSlot, IntList slots, int totalCount) {
            this.itemStack = itemStack;
            this.filterSlot = filterSlot;
            this.slots = slots;
            this.totalCount = totalCount;
        }
    }

    @NotNull
    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(@NotNull IItemHandler inventory) {
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            var matchResult = itemFilterContainer.match(itemStack);
            if (!matchResult.isMatched()) continue;

            if (!result.containsKey(itemStack)) {
                TypeItemInfo itemInfo = new TypeItemInfo(itemStack.copy(), matchResult.getFilterIndex(),
                        new IntArrayList(), 0);
                itemInfo.totalCount += itemStack.getCount();
                itemInfo.slots.add(srcIndex);
                result.put(itemStack.copy(), itemInfo);
            } else {
                TypeItemInfo itemInfo = result.get(itemStack);
                itemInfo.totalCount += itemStack.getCount();
                itemInfo.slots.add(srcIndex);
            }
        }
        return result;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide());
    }

    @Override
    public boolean canInteractWithOutputSide() {
        return true;
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(itemFilterContainer);
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 plateBox, BlockRenderLayer layer) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            Textures.CONVEYOR_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        } else {
            Textures.CONVEYOR_OVERLAY_INVERTED.renderSided(getAttachedSide(), plateBox, renderState, pipeline,
                    translation);
        }
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull RayTraceResult hitResult) {
        if (!getCoverableView().getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != delegate) {
                this.itemHandlerWrapper = new CoverableItemHandlerWrapper(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandlerWrapper);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 192 + 18);

        getItemFilterContainer().setMaxTransferSize(getMaxStackSize());

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createUI(panel, guiSyncManager))
                .bindPlayerInventory();
    }

    protected ParentWidget<Column> createUI(ModularPanel mainPanel, PanelSyncManager guiSyncManager) {
        var column = new Column().top(24).margin(7, 0)
                .widthRel(1f).coverChildrenHeight();

        EnumSyncValue<ManualImportExportMode> manualIOmode = new EnumSyncValue<>(ManualImportExportMode.class,
                this::getManualImportExportMode, this::setManualImportExportMode);

        EnumSyncValue<ConveyorMode> conveyorMode = new EnumSyncValue<>(ConveyorMode.class,
                this::getConveyorMode, this::setConveyorMode);

        IntSyncValue throughput = new IntSyncValue(this::getTransferRate, this::setTransferRate);
        throughput.updateCacheFromSource(true);

        StringSyncValue formattedThroughput = new StringSyncValue(throughput::getStringValue,
                throughput::setStringValue);

        EnumSyncValue<DistributionMode> distributionMode = new EnumSyncValue<>(DistributionMode.class,
                this::getDistributionMode, this::setDistributionMode);

        guiSyncManager.syncValue("manual_io", manualIOmode);
        guiSyncManager.syncValue("conveyor_mode", conveyorMode);
        guiSyncManager.syncValue("distribution_mode", distributionMode);
        guiSyncManager.syncValue("throughput", throughput);

        if (createThroughputRow())
            column.child(new Row().coverChildrenHeight()
                    .marginBottom(2).widthRel(1f)
                    .child(new ButtonWidget<>()
                            .left(0).width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughput.getValue() - getIncrementValue(MouseData.create(mouseButton));
                                throughput.setValue(val, true, true);
                                Interactable.playButtonClickSound();
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                    .child(new TextFieldWidget()
                            .left(18).right(18)
                            .setTextColor(Color.WHITE.darker(1))
                            .setNumbers(1, maxItemTransferRate)
                            .value(formattedThroughput)
                            .background(GTGuiTextures.DISPLAY))
                    .child(new ButtonWidget<>()
                            .right(0).width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughput.getValue() + getIncrementValue(MouseData.create(mouseButton));
                                throughput.setValue(val, true, true);
                                Interactable.playButtonClickSound();
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(true)))));

        if (createFilterRow())
            column.child(getItemFilterContainer().initUI(mainPanel, guiSyncManager));

        if (createManualIOModeRow())
            column.child(new EnumRowBuilder<>(ManualImportExportMode.class)
                    .value(manualIOmode)
                    .lang("cover.generic.manual_io")
                    .overlay(new IDrawable[] {
                            new DynamicDrawable(() -> conveyorMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[0] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[0]),
                            new DynamicDrawable(() -> conveyorMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[1] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[1]),
                            new DynamicDrawable(() -> conveyorMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[2] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[2])
                    })
                    .build());

        if (createConveyorModeRow())
            column.child(new EnumRowBuilder<>(ConveyorMode.class)
                    .value(conveyorMode)
                    .lang("cover.generic.io")
                    .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY)
                    .build());

        if (createDistributionModeRow())
            column.child(new EnumRowBuilder<>(DistributionMode.class)
                    .value(distributionMode)
                    .overlay(16, GTGuiTextures.DISTRIBUTION_MODE_OVERLAY)
                    .lang("cover.generic.distribution.name")
                    .build());

        return column;
    }

    protected boolean createThroughputRow() {
        return true;
    }

    protected boolean createFilterRow() {
        return true;
    }

    protected boolean createManualIOModeRow() {
        return true;
    }

    protected boolean createConveyorModeRow() {
        return true;
    }

    protected boolean createDistributionModeRow() {
        return true;
    }

    protected int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingAllowed;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.isWorkingAllowed = isActivationAllowed;
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_COVER_MODE) {
            this.conveyorMode = buf.readEnumValue(ConveyorMode.class);
            getCoverableView().scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeInt(transferRate);
        packetBuffer.writeByte(conveyorMode.ordinal());
        packetBuffer.writeByte(distributionMode.ordinal());
        packetBuffer.writeByte(manualImportExportMode.ordinal());
        getItemFilterContainer().writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferRate = packetBuffer.readInt();
        this.conveyorMode = ConveyorMode.VALUES[packetBuffer.readByte()];
        this.distributionMode = DistributionMode.VALUES[packetBuffer.readByte()];
        this.manualImportExportMode = ManualImportExportMode.VALUES[packetBuffer.readByte()];
        getItemFilterContainer().readInitialSyncData(packetBuffer);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("ConveyorMode", conveyorMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", this.itemFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.conveyorMode = ConveyorMode.VALUES[tagCompound.getInteger("ConveyorMode")];
        this.distributionMode = DistributionMode.VALUES[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.VALUES[tagCompound.getInteger("ManualImportExportMode")];
        var filterTag = tagCompound.getCompoundTag("Filter");
        if (filterTag.hasKey("IsBlacklist")) {
            this.itemFilterContainer.handleLegacyNBT(filterTag);
        } else {
            this.itemFilterContainer.deserializeNBT(filterTag);
        }
    }

    @Override
    public @NotNull CoverRenderer getRenderer() {
        if (conveyorMode == ConveyorMode.EXPORT) {
            if (renderer == null) renderer = buildRenderer();
            return renderer;
        } else {
            if (rendererInverted == null) rendererInverted = buildRendererInverted();
            return rendererInverted;
        }
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(Textures.CONVEYOR_OVERLAY).setPlateQuads(tier).build();
    }

    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(Textures.CONVEYOR_OVERLAY_INVERTED).setPlateQuads(tier).build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum ConveyorMode implements IStringSerializable, IIOMode {

        IMPORT("cover.conveyor.mode.import"),
        EXPORT("cover.conveyor.mode.export");

        public static final ConveyorMode[] VALUES = values();
        public final String localeName;

        ConveyorMode(String localeName) {
            this.localeName = localeName;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }

        @Override
        public boolean isImport() {
            return this == IMPORT;
        }
    }

    private class CoverableItemHandlerWrapper extends ItemHandlerDelegate {

        public CoverableItemHandlerWrapper(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (conveyorMode == ConveyorMode.EXPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return stack;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED &&
                    !itemFilterContainer.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (conveyorMode == ConveyorMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return ItemStack.EMPTY;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilterContainer.test(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }
}
