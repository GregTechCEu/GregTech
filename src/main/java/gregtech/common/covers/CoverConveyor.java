package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.filter.CoverWithItemFilter;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.NetPath;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.EdgeSelector;
import gregtech.api.graphnet.traverse.NetClosestIterator;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.collection.ListHashSet;
import gregtech.api.util.function.BiIntConsumer;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.covers.filter.MatchResult;
import gregtech.common.covers.filter.MergabilityInfo;
import gregtech.common.pipelike.net.item.ItemCapabilityObject;
import gregtech.common.pipelike.net.item.ItemNetworkView;

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
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.GuiData;
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
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

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

    protected final ObjectLinkedOpenHashSet<IItemHandler> extractionRoundRobinCache = new ObjectLinkedOpenHashSet<>();
    protected final ObjectLinkedOpenHashSet<IItemHandler> insertionRoundRobinCache = new ObjectLinkedOpenHashSet<>();

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
        this.extractionRoundRobinCache.clear();
        this.extractionRoundRobinCache.trim(16);
        this.insertionRoundRobinCache.clear();
        this.insertionRoundRobinCache.trim(16);
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
        ItemFilterContainer filter = this.getItemFilter();
        byFilterSlot = byFilterSlot && filter != null; // can't be by filter slot if there is no filter
        Int2IntLinkedOpenHashMap containedByFilterSlot = new Int2IntLinkedOpenHashMap();
        Int2ObjectArrayMap<MergabilityInfo<ItemTestObject>> filterSlotToMergability = new Int2ObjectArrayMap<>();
        for (int i = 0; i < sourceHandler.getSlots(); i++) {
            ItemStack stack = sourceHandler.getStackInSlot(i);
            int extracted = stack.getCount();
            if (extracted == 0) continue;
            MatchResult match = null;
            if (filter == null || (match = filter.match(stack)).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    filterSlot = match.getFilterIndex();
                }
                containedByFilterSlot.addTo(filterSlot, extracted);
                final int handlerSlot = i;
                filterSlotToMergability.compute(filterSlot, (k, v) -> {
                    if (v == null) v = new MergabilityInfo<>();
                    v.add(handlerSlot, new ItemTestObject(stack), extracted);
                    return v;
                });
            }
        }
        var iter = containedByFilterSlot.int2IntEntrySet().fastIterator();
        int totalTransfer = 0;
        while (iter.hasNext()) {
            var next = iter.next();
            int filterSlot = next.getIntKey();
            int min = Math.max(minTransfer.applyAsInt(filterSlot), 1);
            int max = maxTransfer.applyAsInt(filterSlot);
            if (max < min) continue;
            int slotTransfer = 0;
            if (next.getIntValue() >= min) {
                MergabilityInfo<ItemTestObject> mergabilityInfo = filterSlotToMergability.get(filterSlot);
                var merges = mergabilityInfo.getMerges();
                for (var merge : merges) {
                    if (merge.getCount() + slotTransfer < min) break;
                    int transfer = Math.min(merge.getCount(), max - slotTransfer);
                    transfer = doInsert(destHandler, merge.getTestObject(), transfer, true);
                    if (transfer < min) continue;
                    transfer = doExtract(sourceHandler, merge.getTestObject(), transfer, true);
                    if (transfer < min) continue;
                    doExtract(sourceHandler, merge.getTestObject(), transfer, false);
                    doInsert(destHandler, merge.getTestObject(), transfer, false);
                    int remaining = max - transfer;
                    slotTransfer += transfer;
                    if (remaining <= 0 || slotTransfer >= max) break;
                }
                if (transferReport != null) transferReport.accept(filterSlot, slotTransfer);
                totalTransfer += slotTransfer;
            }
        }
        return totalTransfer;
    }

    protected ObjectLinkedOpenHashSet<IItemHandler> getRoundRobinCache(boolean extract, boolean simulate) {
        ObjectLinkedOpenHashSet<IItemHandler> set = extract ? extractionRoundRobinCache : insertionRoundRobinCache;
        return simulate ? set.clone() : set;
    }

    protected int doExtract(@NotNull IItemHandler handler, ItemTestObject testObject, int count, boolean simulate) {
        ItemCapabilityObject cap;
        if (distributionMode == DistributionMode.FLOOD || (cap = ItemCapabilityObject.instanceOf(handler)) == null)
            return simpleExtract(handler, testObject, count, simulate);
        NetNode origin = cap.getNode();
        // if you find yourself here because you added a new distribution mode and now it won't compile,
        // good luck.
        return switch (distributionMode) {
            case ROUND_ROBIN -> {
                ItemNetworkView view = cap.getNetworkView(ItemCapabilityObject.facingOf(handler));
                Iterator<IItemHandler> iter = view.getHandler().getBackingHandlers().iterator();
                ObjectLinkedOpenHashSet<IItemHandler> cache = getRoundRobinCache(true, simulate);
                Set<IItemHandler> backlog = new ObjectOpenHashSet<>();
                Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(
                        origin.getGroupSafe().getNodes().size());
                int available = count;
                while (available > 0) {
                    if (!cache.isEmpty() && backlog.remove(cache.first())) {
                        IItemHandler candidate = cache.first();
                        NetNode linked = view.getBiMap().get(candidate);
                        if (linked == null) {
                            cache.removeFirst();
                            continue;
                        } else {
                            cache.addAndMoveToLast(candidate);
                        }
                        available = rrExtract(testObject, simulate, origin, flowLimitCache, available, candidate,
                                linked);
                        continue;
                    }
                    if (iter.hasNext()) {
                        IItemHandler candidate = iter.next();
                        boolean frontOfCache = !cache.isEmpty() && cache.first() == candidate;
                        if (frontOfCache || !cache.contains(candidate)) {
                            NetNode linked = view.getBiMap().get(candidate);
                            if (linked == null) {
                                if (frontOfCache) cache.removeFirst();
                                continue;
                            } else {
                                cache.addAndMoveToLast(candidate);
                            }
                            available = rrExtract(testObject, simulate, origin, flowLimitCache, available, candidate,
                                    linked);
                        } else {
                            backlog.add(candidate);
                        }
                    } else if (backlog.isEmpty()) {
                        // we have finished the iterator and backlog
                        break;
                    } else {
                        if (!cache.isEmpty()) {
                            if (view.getHandler().getBackingHandlers().contains(cache.first()))
                                break; // we've already visited the next node in the cache
                            else {
                                // the network view does not contain the node in the front of the cache, so yeet it.
                                cache.removeFirst();
                            }
                        } else {
                            break; // cache is empty and iterator is empty, something is weird, just exit.
                        }
                    }
                }
                while (iter.hasNext()) {
                    cache.add(iter.next());
                }
                yield count - available;
            }
            case EQUALIZED -> {
                // only consider destinations that are not on the other side of a filter that rejects our test object
                NetClosestIterator gather = new NetClosestIterator(origin,
                        EdgeSelector.filtered(EdgeDirection.INCOMING,
                                GraphNetUtility.edgeSelectorBlacklist(testObject)));
                Object2ObjectOpenHashMap<NetNode, IItemHandler> candidates = new Object2ObjectOpenHashMap<>();
                while (gather.hasNext()) {
                    NetNode node = gather.next();
                    if (node instanceof NodeExposingCapabilities exposer) {
                        IItemHandler h = exposer.getProvider().getCapability(
                                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                exposer.exposedFacing());
                        if (h != null && ItemCapabilityObject.instanceOf(h) == null) {
                            candidates.put(node, h);
                        }
                    }
                }
                if (candidates.isEmpty()) yield 0;
                int largestMin = count / candidates.size();
                if (largestMin <= 0) yield 0;
                for (IItemHandler value : candidates.values()) {
                    largestMin = Math.min(largestMin, simpleExtract(value, testObject, largestMin, true));
                    if (largestMin <= 0) yield 0;
                }
                // binary search for largest scale that doesn't exceed flow limits
                Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>();
                Int2ObjectArrayMap<Reference2IntOpenHashMap<NetNode>> flows = new Int2ObjectArrayMap<>();
                largestMin = GTUtility.binarySearchInt(0, largestMin, l -> {
                    if (flows.containsKey(l) && flows.get(l) == null) return false;
                    ResilientNetClosestIterator backwardFrontier = null;
                    Reference2IntOpenHashMap<NetNode> localFlows = new Reference2IntOpenHashMap<>();
                    for (NetNode node : candidates.keySet()) {
                        ListHashSet<NetPath> pathCache = ItemCapabilityObject.getNetworkView(node).getPathCache(origin);
                        ResilientNetClosestIterator forwardFrontier = null;
                        Iterator<NetPath> iterator = pathCache.iterator();
                        int needed = l;
                        while (needed > 0) {
                            NetPath path;
                            if (iterator != null && iterator.hasNext()) path = iterator.next();
                            else {
                                iterator = null;
                                if (backwardFrontier == null) {
                                    backwardFrontier = new ResilientNetClosestIterator(origin, EdgeDirection.INCOMING);
                                }
                                if (forwardFrontier == null) {
                                    forwardFrontier = new ResilientNetClosestIterator(node, EdgeDirection.OUTGOING);
                                }
                                path = GraphNetUtility.p2pNextPath(
                                        n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) <=
                                                localFlows.getInt(n),
                                        e -> !e.test(testObject), forwardFrontier, backwardFrontier,
                                        (f, b) -> f.hasNext());
                                if (path == null) break;
                                int i = pathCache.size();
                                while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                                    i--;
                                }
                                if (!pathCache.addSensitive(i, path)) break;
                            }
                            int extract = attemptPath(path, needed,
                                    n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) -
                                            localFlows.getInt(n),
                                    e -> !e.test(testObject));
                            if (extract > 0) {
                                needed -= extract;
                                ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                                for (int j = 0; j < asList.size(); j++) {
                                    NetNode n = asList.get(j);
                                    localFlows.put(n, localFlows.getInt(n) + extract);
                                }
                            }
                        }
                        if (needed > 0) {
                            flows.put(l, null);
                            return false;
                        }
                    }
                    flows.put(l, localFlows);
                    return true;
                }, false);
                if (largestMin <= 0 || flows.get(largestMin) == null) yield 0;
                if (!simulate) {
                    for (IItemHandler value : candidates.values()) {
                        simpleExtract(value, testObject, largestMin, false);
                    }
                    for (var e : flows.get(largestMin).reference2IntEntrySet()) {
                        ItemCapabilityObject.reportFlow(e.getKey(), e.getIntValue(), testObject);
                    }
                }
                yield largestMin * candidates.size();
            }
            case FLOOD -> 0; // how are you here?
        };
    }

    protected int rrExtract(ItemTestObject testObject, boolean simulate, NetNode origin,
                            Reference2IntOpenHashMap<NetNode> flowLimitCache, int available,
                            IItemHandler candidate, NetNode linked) {
        int extractable = simpleExtract(candidate, testObject, available, true);
        if (extractable > 0) {
            ListHashSet<NetPath> pathCache = ItemCapabilityObject.getNetworkView(linked).getPathCache(origin);
            Iterator<NetPath> iterator = pathCache.iterator();
            ResilientNetClosestIterator forwardFrontier = null;
            ResilientNetClosestIterator backwardFrontier = null;
            while (extractable > 0) {
                NetPath path;
                if (iterator != null && iterator.hasNext()) path = iterator.next();
                else {
                    iterator = null;
                    if (forwardFrontier == null) {
                        forwardFrontier = new ResilientNetClosestIterator(linked, EdgeDirection.OUTGOING);
                        backwardFrontier = new ResilientNetClosestIterator(origin, EdgeDirection.INCOMING);
                    }
                    path = GraphNetUtility.p2pNextPath(
                            n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) <= 0,
                            e -> !e.test(testObject), forwardFrontier, backwardFrontier);
                    if (path == null) break;
                    int i = pathCache.size();
                    while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                        i--;
                    }
                    if (!pathCache.addSensitive(i, path)) break;
                }
                int extract = attemptPath(path, extractable,
                        n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject),
                        e -> !e.test(testObject));
                if (extract > 0) {
                    extractable -= extract;
                    available -= extract;
                    ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                    for (int j = 0; j < asList.size(); j++) {
                        NetNode n = asList.get(j);
                        if (!simulate) ItemCapabilityObject.reportFlow(n, extract, testObject);
                        flowLimitCache.put(n, flowLimitCache.getInt(n) - extract);
                    }
                    if (!simulate) simpleExtract(candidate, testObject, extract, false);
                }
            }
        }
        return available;
    }

    protected int simpleExtract(@NotNull IItemHandler handler, ItemTestObject testObject, int count,
                                boolean simulate) {
        int available = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (testObject.test(slot)) {
                available += handler.extractItem(i, count - available, simulate).getCount();
                if (available == count) return count;
            }
        }
        return available;
    }

    protected int doInsert(@NotNull IItemHandler handler, ItemTestObject testObject, final int count,
                           boolean simulate) {
        ItemCapabilityObject cap;
        if (distributionMode == DistributionMode.FLOOD || (cap = ItemCapabilityObject.instanceOf(handler)) == null)
            return simpleInsert(handler, testObject, count, simulate);
        NetNode origin = cap.getNode();
        // if you find yourself here because you added a new distribution mode and now it won't compile,
        // good luck.
        return switch (distributionMode) {
            case ROUND_ROBIN -> {
                ItemNetworkView view = cap.getNetworkView(ItemCapabilityObject.facingOf(handler));
                Iterator<IItemHandler> iter = view.getHandler().getBackingHandlers().iterator();
                ObjectLinkedOpenHashSet<IItemHandler> cache = getRoundRobinCache(false, simulate);
                Set<IItemHandler> backlog = new ObjectOpenHashSet<>();
                Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>(
                        origin.getGroupSafe().getNodes().size());
                int available = count;
                while (available > 0) {
                    if (!cache.isEmpty() && backlog.remove(cache.first())) {
                        IItemHandler candidate = cache.first();
                        NetNode linked = view.getBiMap().get(candidate);
                        if (linked == null) {
                            cache.removeFirst();
                            continue;
                        } else {
                            cache.addAndMoveToLast(candidate);
                        }
                        available = rrInsert(testObject, simulate, origin, flowLimitCache, available, candidate,
                                linked);
                        continue;
                    }
                    if (iter.hasNext()) {
                        IItemHandler candidate = iter.next();
                        boolean frontOfCache = !cache.isEmpty() && cache.first() == candidate;
                        if (frontOfCache || !cache.contains(candidate)) {
                            NetNode linked = view.getBiMap().get(candidate);
                            if (linked == null) {
                                if (frontOfCache) cache.removeFirst();
                                continue;
                            } else {
                                cache.addAndMoveToLast(candidate);
                            }
                            available = rrInsert(testObject, simulate, origin, flowLimitCache, available, candidate,
                                    linked);
                        } else {
                            backlog.add(candidate);
                        }
                    } else if (backlog.isEmpty()) {
                        // we have finished the iterator and backlog
                        break;
                    } else {
                        if (!cache.isEmpty()) {
                            if (view.getHandler().getBackingHandlers().contains(cache.first()))
                                break; // we've already visited the next node in the cache
                            else {
                                // the network view does not contain the node in the front of the cache, so yeet it.
                                cache.removeFirst();
                            }
                        } else {
                            break; // cache is empty and iterator is empty, something is weird, just exit.
                        }
                    }
                }
                while (iter.hasNext()) {
                    cache.add(iter.next());
                }
                yield count - available;
            }
            case EQUALIZED -> {
                // only consider destinations that are not on the other side of a filter that rejects our test object
                NetClosestIterator gather = new NetClosestIterator(origin,
                        EdgeSelector.filtered(EdgeDirection.OUTGOING,
                                GraphNetUtility.edgeSelectorBlacklist(testObject)));
                Object2ObjectOpenHashMap<NetNode, IItemHandler> candidates = new Object2ObjectOpenHashMap<>();
                while (gather.hasNext()) {
                    NetNode node = gather.next();
                    if (node instanceof NodeExposingCapabilities exposer) {
                        IItemHandler h = exposer.getProvider().getCapability(
                                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                exposer.exposedFacing());
                        if (h != null && ItemCapabilityObject.instanceOf(h) == null) {
                            candidates.put(node, h);
                        }
                    }
                }
                if (candidates.isEmpty()) yield 0;
                int largestMin = count / candidates.size();
                if (largestMin <= 0) yield 0;
                for (IItemHandler value : candidates.values()) {
                    largestMin = Math.min(largestMin, simpleInsert(value, testObject, largestMin, true));
                    if (largestMin <= 0) yield 0;
                }
                // binary search for largest scale that doesn't exceed flow limits
                Reference2IntOpenHashMap<NetNode> flowLimitCache = new Reference2IntOpenHashMap<>();
                Int2ObjectArrayMap<Reference2IntOpenHashMap<NetNode>> flows = new Int2ObjectArrayMap<>();
                ItemNetworkView view = ItemCapabilityObject.getNetworkView(origin);
                largestMin = GTUtility.binarySearchInt(0, largestMin, l -> {
                    if (flows.containsKey(l) && flows.get(l) == null) return false;
                    ResilientNetClosestIterator forwardFrontier = null;
                    Reference2IntOpenHashMap<NetNode> localFlows = new Reference2IntOpenHashMap<>();
                    for (NetNode node : candidates.keySet()) {
                        ListHashSet<NetPath> pathCache = view.getPathCache(node);
                        ResilientNetClosestIterator backwardFrontier = null;
                        Iterator<NetPath> iterator = pathCache.iterator();
                        int needed = l;
                        while (needed > 0) {
                            NetPath path;
                            if (iterator != null && iterator.hasNext()) path = iterator.next();
                            else {
                                iterator = null;
                                if (forwardFrontier == null) {
                                    forwardFrontier = new ResilientNetClosestIterator(origin, EdgeDirection.OUTGOING);
                                }
                                if (backwardFrontier == null) {
                                    backwardFrontier = new ResilientNetClosestIterator(node, EdgeDirection.INCOMING);
                                }
                                path = GraphNetUtility.p2pNextPath(
                                        n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) <=
                                                localFlows.getInt(n),
                                        e -> !e.test(testObject), forwardFrontier, backwardFrontier,
                                        (f, b) -> b.hasNext());
                                if (path == null) break;
                                int i = pathCache.size();
                                while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                                    i--;
                                }
                                if (!pathCache.addSensitive(i, path)) break;
                            }
                            int insert = attemptPath(path, needed,
                                    n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) -
                                            localFlows.getInt(n),
                                    e -> !e.test(testObject));
                            if (insert > 0) {
                                needed -= insert;
                                ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                                for (int j = 0; j < asList.size(); j++) {
                                    NetNode n = asList.get(j);
                                    localFlows.put(n, localFlows.getInt(n) + insert);
                                }
                            }
                        }
                        if (needed > 0) {
                            flows.put(l, null);
                            return false;
                        }
                    }
                    flows.put(l, localFlows);
                    return true;
                }, false);
                if (largestMin <= 0 || flows.get(largestMin) == null) yield 0;
                if (!simulate) {
                    for (IItemHandler value : candidates.values()) {
                        simpleInsert(value, testObject, largestMin, false);
                    }
                    for (var e : flows.get(largestMin).reference2IntEntrySet()) {
                        ItemCapabilityObject.reportFlow(e.getKey(), e.getIntValue(), testObject);
                    }
                }
                yield largestMin * candidates.size();
            }
            case FLOOD -> 0; // how are you here?
        };
    }

    protected int rrInsert(ItemTestObject testObject, boolean simulate, NetNode origin,
                           Reference2IntOpenHashMap<NetNode> flowLimitCache, int available, IItemHandler candidate,
                           NetNode linked) {
        int insertable = simpleInsert(candidate, testObject, available, true);
        if (insertable > 0) {
            ListHashSet<NetPath> pathCache = ItemCapabilityObject.getNetworkView(origin).getPathCache(linked);
            Iterator<NetPath> iterator = pathCache.iterator();
            ResilientNetClosestIterator forwardFrontier = null;
            ResilientNetClosestIterator backwardFrontier = null;
            while (insertable > 0) {
                NetPath path;
                if (iterator != null && iterator.hasNext()) path = iterator.next();
                else {
                    iterator = null;
                    if (forwardFrontier == null) {
                        forwardFrontier = new ResilientNetClosestIterator(origin, EdgeDirection.OUTGOING);
                        backwardFrontier = new ResilientNetClosestIterator(linked, EdgeDirection.INCOMING);
                    }
                    path = GraphNetUtility.p2pNextPath(
                            n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject) <= 0,
                            e -> !e.test(testObject), forwardFrontier, backwardFrontier);
                    if (path == null) break;
                    int i = pathCache.size();
                    while (i > 0 && pathCache.get(i - 1).getWeight() > path.getWeight()) {
                        i--;
                    }
                    if (!pathCache.addSensitive(i, path)) break;
                }
                int insert = attemptPath(path, insertable,
                        n -> ItemCapabilityObject.getFlowLimitCached(flowLimitCache, n, testObject),
                        e -> !e.test(testObject));
                if (insert > 0) {
                    insertable -= insert;
                    available -= insert;
                    ImmutableList<NetNode> asList = path.getOrderedNodes().asList();
                    for (int j = 0; j < asList.size(); j++) {
                        NetNode n = asList.get(j);
                        if (!simulate) ItemCapabilityObject.reportFlow(n, insert, testObject);
                        flowLimitCache.put(n, flowLimitCache.getInt(n) - insert);
                    }
                    if (!simulate) simpleInsert(candidate, testObject, insert, false);
                }
            }
        }
        return available;
    }

    protected int simpleInsert(@NotNull IItemHandler handler, ItemTestObject testObject, int count,
                               boolean simulate) {
        int available = count;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack toInsert = testObject.recombine(Math.min(available, handler.getSlotLimit(i)));
            available -= toInsert.getCount() - handler.insertItem(i, toInsert, simulate).getCount();
            if (available <= 0) return count;
        }
        return count - available;
    }

    protected int attemptPath(NetPath path, int available, ToIntFunction<NetNode> limit, Predicate<NetEdge> filter) {
        ImmutableList<NetEdge> edges = path.getOrderedEdges().asList();
        for (int i = 0; i < edges.size(); i++) {
            if (filter.test(edges.get(i))) return 0;
        }
        ImmutableList<NetNode> nodes = path.getOrderedNodes().asList();
        for (int i = 0; i < nodes.size(); i++) {
            available = Math.min(limit.applyAsInt(nodes.get(i)), available);
            if (available <= 0) return 0;
        }
        return available;
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
                .child(createUI(guiData, guiSyncManager))
                .bindPlayerInventory();
    }

    protected ParentWidget<Flow> createUI(GuiData data, PanelSyncManager guiSyncManager) {
        var column = Flow.column().top(24).margin(7, 0)
                .widthRel(1f).coverChildrenHeight();

        EnumSyncValue<ManualImportExportMode> manualIOmode = new EnumSyncValue<>(ManualImportExportMode.class,
                this::getManualImportExportMode, this::setManualImportExportMode);

        EnumSyncValue<ConveyorMode> conveyorMode = new EnumSyncValue<>(ConveyorMode.class,
                this::getConveyorMode, this::setConveyorMode);

        IntSyncValue throughput = new IntSyncValue(this::getTransferRate, this::setTransferRate);

        StringSyncValue formattedThroughput = new StringSyncValue(throughput::getStringValue,
                throughput::setStringValue);

        EnumSyncValue<DistributionMode> distributionMode = new EnumSyncValue<>(DistributionMode.class,
                this::getDistributionMode, this::setDistributionMode);

        guiSyncManager.syncValue("manual_io", manualIOmode);
        guiSyncManager.syncValue("conveyor_mode", conveyorMode);
        guiSyncManager.syncValue("distribution_mode", distributionMode);
        guiSyncManager.syncValue("throughput", throughput);

        if (createThroughputRow())
            column.child(Flow.row().coverChildrenHeight()
                    .marginBottom(2).widthRel(1f)
                    .child(new ButtonWidget<>()
                            .left(0).width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughput.getValue() - getIncrementValue(MouseData.create(mouseButton));
                                throughput.setValue(val, true, true);
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
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(true)))));

        if (createFilterRow())
            column.child(getItemFilterContainer().initUI(data, guiSyncManager));

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
