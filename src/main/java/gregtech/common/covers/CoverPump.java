package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FluidHandlerDelegate;
import gregtech.api.capability.impl.FluidHandlerList;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.filter.CoverWithFluidFilter;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.EdgeSelector;
import gregtech.api.graphnet.traverse.NetClosestIterator;
import gregtech.api.graphnet.traverse.ResilientNetClosestIterator;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.BiIntConsumer;
import gregtech.client.renderer.pipe.cover.CoverRenderer;
import gregtech.client.renderer.pipe.cover.CoverRendererBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.covers.filter.MatchResult;
import gregtech.common.pipelike.net.fluid.FluidCapabilityObject;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public class CoverPump extends CoverBase implements CoverWithUI, ITickable, IControllable, CoverWithFluidFilter {

    public final int tier;
    public final int maxFluidTransferRate;
    protected int transferRate;
    protected PumpMode pumpMode = PumpMode.EXPORT;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected DistributionMode distributionMode = DistributionMode.FLOOD;
    protected int fluidLeftToTransferLastSecond;
    private CoverableFluidHandlerWrapper fluidHandlerWrapper;
    protected boolean isWorkingAllowed = true;
    protected FluidFilterContainer fluidFilterContainer;
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;

    protected final ObjectLinkedOpenHashSet<IFluidHandler> extractionRoundRobinCache = new ObjectLinkedOpenHashSet<>();
    protected final ObjectLinkedOpenHashSet<IFluidHandler> insertionRoundRobinCache = new ObjectLinkedOpenHashSet<>();

    protected @Nullable CoverRenderer rendererInverted;

    public CoverPump(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                     @NotNull EnumFacing attachedSide, int tier, int mbPerTick) {
        super(definition, coverableView, attachedSide);
        this.tier = tier;
        this.maxFluidTransferRate = mbPerTick;
        this.transferRate = mbPerTick;
        this.fluidLeftToTransferLastSecond = transferRate;
        this.fluidFilterContainer = new FluidFilterContainer(this);
    }

    @Override
    public @Nullable FluidFilterContainer getFluidFilter() {
        return this.fluidFilterContainer;
    }

    @Override
    public FluidFilterMode getFilterMode() {
        return FluidFilterMode.FILTER_BOTH;
    }

    @Override
    public ManualImportExportMode getManualMode() {
        return this.manualImportExportMode;
    }

    public void setStringTransferRate(String s) {
        this.fluidFilterContainer.setTransferSize(
                getBucketMode() == BucketMode.MILLI_BUCKET ?
                        Integer.parseInt(s) :
                        Integer.parseInt(s) * 1000);
    }

    public String getStringTransferRate() {
        return String.valueOf(getBucketMode() == BucketMode.MILLI_BUCKET ?
                this.fluidFilterContainer.getTransferSize() :
                this.fluidFilterContainer.getTransferSize() / 1000);
    }

    public void setTransferRate(int transferRate) {
        if (bucketMode == BucketMode.BUCKET) transferRate *= 1000;
        this.transferRate = MathHelper.clamp(transferRate, 1, maxFluidTransferRate);
        markDirty();
    }

    public int getTransferRate() {
        return bucketMode == BucketMode.BUCKET ? transferRate / 1000 : transferRate;
    }

    protected void adjustTransferRate(int amount) {
        amount *= this.bucketMode == BucketMode.BUCKET ? 1000 : 1;
        setTransferRate(this.transferRate + amount);
    }

    public void setPumpMode(PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        writeCustomData(GregtechDataCodes.UPDATE_COVER_MODE, buf -> buf.writeEnumValue(pumpMode));
        markDirty();
    }

    public PumpMode getPumpMode() {
        return pumpMode;
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

    public void setBucketMode(BucketMode bucketMode) {
        this.bucketMode = bucketMode;
        if (this.bucketMode == BucketMode.BUCKET)
            setTransferRate(transferRate / 1000 * 1000);
        markDirty();
    }

    public BucketMode getBucketMode() {
        return bucketMode;
    }

    public ManualImportExportMode getManualImportExportMode() {
        return manualImportExportMode;
    }

    protected void setManualImportExportMode(ManualImportExportMode manualImportExportMode) {
        this.manualImportExportMode = manualImportExportMode;
        markDirty();
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return fluidFilterContainer;
    }

    @Override
    public void update() {
        long timer = getOffsetTimer();
        if (isWorkingAllowed && getFluidsLeftToTransfer() > 0) {
            EnumFacing side = getAttachedSide();
            TileEntity tileEntity = getNeighbor(side);
            IFluidHandler fluidHandler = tileEntity == null ? null : tileEntity
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            IFluidHandler myFluidHandler = getCoverableView().getCapability(
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if (myFluidHandler != null && fluidHandler != null) {
                if (pumpMode == PumpMode.EXPORT) {
                    performTransferOnUpdate(myFluidHandler, fluidHandler);
                } else {
                    performTransferOnUpdate(fluidHandler, myFluidHandler);
                }
            }
        }
        if (timer % 20 == 0) {
            refreshBuffer(transferRate);
        }
    }

    public int getFluidsLeftToTransfer() {
        return fluidLeftToTransferLastSecond;
    }

    public void reportFluidsTransfer(int transferred) {
        fluidLeftToTransferLastSecond -= transferred;
    }

    protected void refreshBuffer(int transferRate) {
        this.fluidLeftToTransferLastSecond = transferRate;
    }

    protected void performTransferOnUpdate(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler) {
        reportFluidsTransfer(performTransfer(sourceHandler, destHandler, false, i -> 0,
                i -> getFluidsLeftToTransfer(), null));
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
    protected int performTransfer(@NotNull IFluidHandler sourceHandler, @NotNull IFluidHandler destHandler,
                                  boolean byFilterSlot, @NotNull IntUnaryOperator minTransfer,
                                  @NotNull IntUnaryOperator maxTransfer, @Nullable BiIntConsumer transferReport) {
        FluidFilterContainer filter = this.getFluidFilter();
        byFilterSlot = byFilterSlot && filter != null; // can't be by filter slot if there is no filter
        Object2IntOpenHashMap<FluidTestObject> contained = new Object2IntOpenHashMap<>();
        var tanks = sourceHandler.getTankProperties();
        for (IFluidTankProperties tank : tanks) {
            FluidStack contents = tank.getContents();
            if (contents != null) contained.merge(new FluidTestObject(contents), contents.amount, Integer::sum);
        }
        var iter = contained.object2IntEntrySet().fastIterator();
        int totalTransfer = 0;
        while (iter.hasNext()) {
            var content = iter.next();
            MatchResult match = null;
            if (filter == null ||
                    (match = filter.match(content.getKey().recombine(content.getIntValue()))).isMatched()) {
                int filterSlot = -1;
                if (byFilterSlot) {
                    assert filter != null; // we know it is not null, because if it were byFilterSlot would be false.
                    filterSlot = match.getFilterIndex();
                }
                int min = Math.max(minTransfer.applyAsInt(filterSlot), 1);
                int max = maxTransfer.applyAsInt(filterSlot);
                if (max < min) continue;

                if (content.getIntValue() < min) continue;
                int transfer = Math.min(content.getIntValue(), max);
                transfer = doInsert(destHandler, content.getKey(), transfer, true);
                if (transfer < min) continue;
                transfer = doExtract(sourceHandler, content.getKey(), transfer, true);
                if (transfer < min) continue;
                doExtract(sourceHandler, content.getKey(), transfer, false);
                doInsert(destHandler, content.getKey(), transfer, false);
                if (transferReport != null) transferReport.accept(filterSlot, transfer);
                totalTransfer += transfer;
            }
        }
        return totalTransfer;
    }

    protected ObjectLinkedOpenHashSet<IFluidHandler> getRoundRobinCache(boolean extract, boolean simulate) {
        ObjectLinkedOpenHashSet<IFluidHandler> set = extract ? extractionRoundRobinCache : insertionRoundRobinCache;
        return simulate ? set.clone() : set;
    }

    protected int doExtract(@NotNull IFluidHandler handler, FluidTestObject testObject, int count,
                            boolean simulate) {
        FluidCapabilityObject cap;
        if (distributionMode == DistributionMode.FLOOD || (cap = FluidCapabilityObject.instanceOf(handler)) == null)
            return simpleExtract(handler, testObject, count, simulate);
        NetNode origin = cap.getNode();
        Predicate<Object> filter = GraphNetUtility.standardEdgeBlacklist(testObject);
        // if you find yourself here because you added a new distribution mode and now it won't compile,
        // good luck.
        return switch (distributionMode) {
            case ROUND_ROBIN -> {
                FluidHandlerList view = cap.getNetworkView();
                Iterator<IFluidHandler> iter = view.getBackingHandlers().iterator();
                ObjectLinkedOpenHashSet<IFluidHandler> cache = getRoundRobinCache(true, simulate);
                Set<IFluidHandler> backlog = new ObjectOpenHashSet<>();
                Object2IntOpenHashMap<NetNode> flows = new Object2IntOpenHashMap<>();
                int available = count;
                while (available > 0) {
                    if (!cache.isEmpty() && backlog.remove(cache.first())) {
                        IFluidHandler candidate = cache.first();
                        NetNode linked = cap.getHandlerToNodeMap().get(candidate);
                        if (linked == null) {
                            cache.removeFirst();
                            continue;
                        } else {
                            cache.addAndMoveToLast(candidate);
                        }
                        available = rrExtract(testObject, simulate, origin, filter, flows, available, candidate,
                                linked);
                        continue;
                    }
                    if (iter.hasNext()) {
                        IFluidHandler candidate = iter.next();
                        boolean frontOfCache = !cache.isEmpty() && cache.first() == candidate;
                        if (frontOfCache || !cache.contains(candidate)) {
                            NetNode linked = cap.getHandlerToNodeMap().get(candidate);
                            if (linked == null) {
                                if (frontOfCache) cache.removeFirst();
                                continue;
                            } else {
                                cache.addAndMoveToLast(candidate);
                            }
                            available = rrExtract(testObject, simulate, origin, filter, flows, available, candidate,
                                    linked);
                        } else {
                            backlog.add(candidate);
                        }
                    } else if (backlog.isEmpty()) {
                        // we have finished the iterator and backlog
                        break;
                    } else {
                        if (!cache.isEmpty()) {
                            if (view.getBackingHandlers().contains(cache.first()))
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
                if (!simulate) {
                    for (var entry : flows.object2IntEntrySet()) {
                        FluidCapabilityObject.reportFlow(entry.getKey(), entry.getIntValue(), testObject);
                    }
                }
                yield count - available;
            }
            case EQUALIZED -> {
                NetClosestIterator gather = new NetClosestIterator(origin,
                        EdgeSelector.filtered(EdgeDirection.INCOMING, filter));
                Object2ObjectOpenHashMap<NetNode, IFluidHandler> candidates = new Object2ObjectOpenHashMap<>();
                while (gather.hasNext()) {
                    NetNode node = gather.next();
                    if (node instanceof NodeExposingCapabilities exposer) {
                        IFluidHandler h = exposer.getProvider().getCapability(
                                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                                exposer.exposedFacing());
                        if (h != null && FluidCapabilityObject.instanceOf(h) == null) {
                            candidates.put(node, h);
                        }
                    }
                }
                int largestMin = count / candidates.size();
                if (largestMin <= 0) yield 0;
                for (IFluidHandler value : candidates.values()) {
                    largestMin = Math.min(largestMin, simpleExtract(value, testObject, largestMin, true));
                    if (largestMin <= 0) yield 0;
                }
                // binary search for largest scale that doesn't exceed flow limits
                Int2ObjectArrayMap<Object2IntOpenHashMap<NetNode>> flows = new Int2ObjectArrayMap<>();
                largestMin = GTUtility.binarySearchInt(0, largestMin, l -> {
                    if (flows.containsKey(l) && flows.get(l) == null) return false;
                    ResilientNetClosestIterator forwardFrontier = new ResilientNetClosestIterator(origin,
                            EdgeSelector.filtered(EdgeDirection.INCOMING, filter));
                    Object2IntOpenHashMap<NetNode> localFlows = new Object2IntOpenHashMap<>();
                    for (NetNode node : candidates.keySet()) {
                        ResilientNetClosestIterator backwardFrontier = new ResilientNetClosestIterator(node,
                                EdgeSelector.filtered(EdgeDirection.OUTGOING, filter));
                        if (GraphNetUtility.p2pWalk(simulate, l,
                                n -> FluidCapabilityObject.getFlowLimit(n, testObject) - localFlows.getInt(n),
                                (n, i) -> localFlows.put(n, localFlows.getInt(n) + i),
                                forwardFrontier, backwardFrontier) < l)
                            return false;
                    }
                    flows.put(l, localFlows);
                    return true;
                }, false);
                if (largestMin <= 0 || flows.get(largestMin) == null) yield 0;
                if (!simulate) {
                    for (IFluidHandler value : candidates.values()) {
                        simpleExtract(value, testObject, largestMin, false);
                    }
                    for (var e : flows.get(largestMin).object2IntEntrySet()) {
                        FluidCapabilityObject.reportFlow(e.getKey(), e.getIntValue(), testObject);
                    }
                }
                yield largestMin * candidates.size();
            }
            case FLOOD -> 0; // how are you here?
        };
    }

    protected int rrExtract(FluidTestObject testObject, boolean simulate, NetNode origin, Predicate<Object> filter,
                            Object2IntOpenHashMap<NetNode> flows, int available, IFluidHandler candidate,
                            NetNode linked) {
        int accepted = simpleExtract(candidate, testObject, available, true);
        if (accepted > 0) {
            ResilientNetClosestIterator forwardFrontier = new ResilientNetClosestIterator(origin,
                    EdgeSelector.filtered(EdgeDirection.INCOMING, filter));
            ResilientNetClosestIterator backwardFrontier = new ResilientNetClosestIterator(linked,
                    EdgeSelector.filtered(EdgeDirection.OUTGOING, filter));
            accepted = GraphNetUtility.p2pWalk(simulate, accepted,
                    n -> FluidCapabilityObject.getFlowLimit(n, testObject) - flows.getInt(n),
                    (n, i) -> flows.put(n, flows.getInt(n) + i),
                    forwardFrontier, backwardFrontier);
            if (accepted > 0) {
                available -= accepted;
                if (!simulate) simpleExtract(candidate, testObject, accepted, false);
            }
        }
        return available;
    }

    protected int simpleExtract(@NotNull IFluidHandler destHandler, FluidTestObject testObject, int count,
                                boolean simulate) {
        FluidStack ext = destHandler.drain(testObject.recombine(count), !simulate);
        return ext == null ? 0 : ext.amount;
    }

    protected int doInsert(@NotNull IFluidHandler handler, FluidTestObject testObject, int count,
                           boolean simulate) {
        FluidCapabilityObject cap;
        if (distributionMode == DistributionMode.FLOOD || (cap = FluidCapabilityObject.instanceOf(handler)) == null)
            return simpleInsert(handler, testObject, count, simulate);
        NetNode origin = cap.getNode();
        Predicate<Object> filter = GraphNetUtility.standardEdgeBlacklist(testObject);
        // if you find yourself here because you added a new distribution mode and now it won't compile,
        // good luck.
        return switch (distributionMode) {
            case ROUND_ROBIN -> {
                FluidHandlerList view = cap.getNetworkView();
                Iterator<IFluidHandler> iter = view.getBackingHandlers().iterator();
                ObjectLinkedOpenHashSet<IFluidHandler> cache = getRoundRobinCache(false, simulate);
                Set<IFluidHandler> backlog = new ObjectOpenHashSet<>();
                Object2IntOpenHashMap<NetNode> flows = new Object2IntOpenHashMap<>();
                int available = count;
                while (available > 0) {
                    if (!cache.isEmpty() && backlog.remove(cache.first())) {
                        IFluidHandler candidate = cache.first();
                        NetNode linked = cap.getHandlerToNodeMap().get(candidate);
                        if (linked == null) {
                            cache.removeFirst();
                            continue;
                        } else {
                            cache.addAndMoveToLast(candidate);
                        }
                        available = rrInsert(testObject, simulate, origin, filter, flows, available, candidate, linked);
                        continue;
                    }
                    if (iter.hasNext()) {
                        IFluidHandler candidate = iter.next();
                        boolean frontOfCache = !cache.isEmpty() && cache.first() == candidate;
                        if (frontOfCache || !cache.contains(candidate)) {
                            NetNode linked = cap.getHandlerToNodeMap().get(candidate);
                            if (linked == null) {
                                if (frontOfCache) cache.removeFirst();
                                continue;
                            } else {
                                cache.addAndMoveToLast(candidate);
                            }
                            available = rrInsert(testObject, simulate, origin, filter, flows, available, candidate,
                                    linked);
                        } else {
                            backlog.add(candidate);
                        }
                    } else if (backlog.isEmpty()) {
                        // we have finished the iterator and backlog
                        break;
                    } else {
                        if (!cache.isEmpty()) {
                            if (view.getBackingHandlers().contains(cache.first()))
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
                if (!simulate) {
                    for (var entry : flows.object2IntEntrySet()) {
                        FluidCapabilityObject.reportFlow(entry.getKey(), entry.getIntValue(), testObject);
                    }
                }
                yield count - available;
            }
            case EQUALIZED -> {
                NetClosestIterator gather = new NetClosestIterator(origin,
                        EdgeSelector.filtered(EdgeDirection.OUTGOING, filter));
                Object2ObjectOpenHashMap<NetNode, IFluidHandler> candidates = new Object2ObjectOpenHashMap<>();
                while (gather.hasNext()) {
                    NetNode node = gather.next();
                    if (node instanceof NodeExposingCapabilities exposer) {
                        IFluidHandler h = exposer.getProvider().getCapability(
                                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                                exposer.exposedFacing());
                        if (h != null && FluidCapabilityObject.instanceOf(h) == null) {
                            candidates.put(node, h);
                        }
                    }
                }
                int largestMin = count / candidates.size();
                if (largestMin <= 0) yield 0;
                for (IFluidHandler value : candidates.values()) {
                    largestMin = Math.min(largestMin, simpleInsert(value, testObject, largestMin, true));
                    if (largestMin <= 0) yield 0;
                }
                // binary search for largest scale that doesn't exceed flow limits
                Int2ObjectArrayMap<Object2IntOpenHashMap<NetNode>> flows = new Int2ObjectArrayMap<>();
                largestMin = GTUtility.binarySearchInt(0, largestMin, l -> {
                    if (flows.containsKey(l) && flows.get(l) == null) return false;
                    ResilientNetClosestIterator forwardFrontier = new ResilientNetClosestIterator(origin,
                            EdgeSelector.filtered(EdgeDirection.OUTGOING, filter));
                    Object2IntOpenHashMap<NetNode> localFlows = new Object2IntOpenHashMap<>();
                    for (NetNode node : candidates.keySet()) {
                        ResilientNetClosestIterator backwardFrontier = new ResilientNetClosestIterator(node,
                                EdgeSelector.filtered(EdgeDirection.INCOMING, filter));
                        if (GraphNetUtility.p2pWalk(simulate, l,
                                n -> FluidCapabilityObject.getFlowLimit(n, testObject) - localFlows.getInt(n),
                                (n, i) -> localFlows.put(n, localFlows.getInt(n) + i),
                                forwardFrontier, backwardFrontier) < l)
                            return false;
                    }
                    flows.put(l, localFlows);
                    return true;
                }, false);
                if (largestMin <= 0 || flows.get(largestMin) == null) yield 0;
                if (!simulate) {
                    for (IFluidHandler value : candidates.values()) {
                        simpleInsert(value, testObject, largestMin, false);
                    }
                    for (var e : flows.get(largestMin).object2IntEntrySet()) {
                        FluidCapabilityObject.reportFlow(e.getKey(), e.getIntValue(), testObject);
                    }
                }
                yield largestMin * candidates.size();
            }
            case FLOOD -> 0; // how are you here?
        };
    }

    protected int rrInsert(FluidTestObject testObject, boolean simulate, NetNode origin, Predicate<Object> filter,
                           Object2IntOpenHashMap<NetNode> flows, int available, IFluidHandler candidate,
                           NetNode linked) {
        int accepted = simpleInsert(candidate, testObject, available, true);
        if (accepted > 0) {
            ResilientNetClosestIterator forwardFrontier = new ResilientNetClosestIterator(origin,
                    EdgeSelector.filtered(EdgeDirection.OUTGOING, filter));
            ResilientNetClosestIterator backwardFrontier = new ResilientNetClosestIterator(linked,
                    EdgeSelector.filtered(EdgeDirection.INCOMING, filter));
            accepted = GraphNetUtility.p2pWalk(simulate, accepted,
                    n -> FluidCapabilityObject.getFlowLimit(n, testObject) - flows.getInt(n),
                    (n, i) -> flows.put(n, flows.getInt(n) + i),
                    forwardFrontier, backwardFrontier);
            if (accepted > 0) {
                available -= accepted;
                if (!simulate) simpleInsert(candidate, testObject, accepted, false);
            }
        }
        return available;
    }

    protected int simpleInsert(@NotNull IFluidHandler destHandler, FluidTestObject testObject, int count,
                               boolean simulate) {
        return destHandler.fill(testObject.recombine(count), !simulate);
    }

    protected boolean checkInputFluid(FluidStack fluidStack) {
        return fluidFilterContainer.test(fluidStack);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 192 + 18);

        getFluidFilterContainer().setMaxTransferSize(getMaxTransferRate());

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createUI(panel, guiSyncManager))
                .bindPlayerInventory();
    }

    protected ParentWidget<?> createUI(ModularPanel mainPanel, PanelSyncManager syncManager) {
        var manualIOmode = new EnumSyncValue<>(ManualImportExportMode.class,
                this::getManualImportExportMode, this::setManualImportExportMode);
        manualIOmode.updateCacheFromSource(true);

        var throughput = new IntSyncValue(this::getTransferRate, this::setTransferRate);
        throughput.updateCacheFromSource(true);

        var throughputString = new StringSyncValue(
                throughput::getStringValue,
                throughput::setStringValue);
        throughputString.updateCacheFromSource(true);

        var pumpMode = new EnumSyncValue<>(PumpMode.class, this::getPumpMode, this::setPumpMode);
        pumpMode.updateCacheFromSource(true);

        EnumSyncValue<DistributionMode> distributionMode = new EnumSyncValue<>(DistributionMode.class,
                this::getDistributionMode, this::setDistributionMode);

        syncManager.syncValue("manual_io", manualIOmode);
        syncManager.syncValue("pump_mode", pumpMode);
        syncManager.syncValue("distribution_mode", distributionMode);
        syncManager.syncValue("throughput", throughput);

        var column = new Column().top(24).margin(7, 0)
                .widthRel(1f).coverChildrenHeight();

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
                            .setNumbers(1, maxFluidTransferRate)
                            .value(throughputString)
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
            column.child(getFluidFilterContainer()
                    .initUI(mainPanel, syncManager));

        if (createManualIOModeRow())
            column.child(new EnumRowBuilder<>(ManualImportExportMode.class)
                    .value(manualIOmode)
                    .lang("cover.generic.manual_io")
                    .overlay(new IDrawable[] {
                            new DynamicDrawable(() -> pumpMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[0] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[0]),
                            new DynamicDrawable(() -> pumpMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[1] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[1]),
                            new DynamicDrawable(() -> pumpMode.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[2] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[2])
                    })
                    .build());

        if (createPumpModeRow())
            column.child(new EnumRowBuilder<>(PumpMode.class)
                    .value(pumpMode)
                    .lang("cover.pump.mode")
                    .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY) // todo pump mode overlays
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

    protected boolean createPumpModeRow() {
        return true;
    }

    protected boolean createDistributionModeRow() {
        return true;
    }

    protected int getMaxTransferRate() {
        return 1;
    }

    @Override
    public @NotNull EnumActionResult onScrewdriverClick(@NotNull EntityPlayer playerIn, @NotNull EnumHand hand,
                                                        @NotNull RayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void readCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.readCustomData(discriminator, buf);
        if (discriminator == GregtechDataCodes.UPDATE_COVER_MODE) {
            this.pumpMode = buf.readEnumValue(PumpMode.class);
            scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeByte(pumpMode.ordinal());
        getFluidFilterContainer().writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.pumpMode = PumpMode.VALUES[packetBuffer.readByte()];
        getFluidFilterContainer().readInitialSyncData(packetBuffer);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public boolean canInteractWithOutputSide() {
        return true;
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(fluidFilterContainer);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        if (pumpMode == PumpMode.EXPORT) {
            Textures.PUMP_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        } else {
            Textures.PUMP_OVERLAY_INVERTED.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    public @NotNull CoverRenderer getRenderer() {
        if (pumpMode == PumpMode.EXPORT) {
            if (renderer == null) renderer = buildRenderer();
            return renderer;
        } else {
            if (rendererInverted == null) rendererInverted = buildRendererInverted();
            return rendererInverted;
        }
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(Textures.PUMP_OVERLAY).setPlateQuads(tier).build();
    }

    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(Textures.PUMP_OVERLAY_INVERTED).setPlateQuads(tier).build();
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IFluidHandler delegate = (IFluidHandler) defaultValue;
            if (fluidHandlerWrapper == null || fluidHandlerWrapper.delegate != delegate) {
                this.fluidHandlerWrapper = new CoverableFluidHandlerWrapper(delegate);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlerWrapper);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
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
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setInteger("BucketMode", bucketMode.ordinal());
        tagCompound.setTag("Filter", fluidFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.pumpMode = PumpMode.VALUES[tagCompound.getInteger("PumpMode")];
        this.distributionMode = DistributionMode.VALUES[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.VALUES[tagCompound.getInteger("ManualImportExportMode")];
        this.bucketMode = BucketMode.VALUES[tagCompound.getInteger("BucketMode")];
        var filterTag = tagCompound.getCompoundTag("Filter");
        if (filterTag.hasKey("IsBlacklist"))
            this.fluidFilterContainer.handleLegacyNBT(filterTag);
        else
            this.fluidFilterContainer.deserializeNBT(filterTag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum PumpMode implements IStringSerializable, IIOMode {

        IMPORT("cover.pump.mode.import"),
        EXPORT("cover.pump.mode.export");

        public static final PumpMode[] VALUES = values();
        public final String localeName;

        PumpMode(String localeName) {
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

    public enum BucketMode implements IStringSerializable {

        BUCKET("cover.bucket.mode.bucket"),
        MILLI_BUCKET("cover.bucket.mode.milli_bucket");

        public static final BucketMode[] VALUES = values();
        public final String localeName;

        BucketMode(String localeName) {
            this.localeName = localeName;
        }

        @NotNull
        @Override
        public String getName() {
            return localeName;
        }
    }

    private class CoverableFluidHandlerWrapper extends FluidHandlerDelegate {

        public CoverableFluidHandlerWrapper(@NotNull IFluidHandler delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (pumpMode == PumpMode.EXPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return 0;
            }
            if (!checkInputFluid(resource) && manualImportExportMode == ManualImportExportMode.FILTERED) {
                return 0;
            }
            return super.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (pumpMode == PumpMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return null;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED && !checkInputFluid(resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (pumpMode == PumpMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return null;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED) {
                FluidStack result = super.drain(maxDrain, false);
                if (result == null || result.amount <= 0 || !checkInputFluid(result)) {
                    return null;
                }
                return doDrain ? super.drain(maxDrain, true) : result;
            }
            return super.drain(maxDrain, doDrain);
        }
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }
}
