package gregtech.common.covers;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CoverConveyor extends CoverBase implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxItemTransferRate;
    private int transferRate;
    protected ConveyorMode conveyorMode;
    protected DistributionMode distributionMode;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected final ItemFilterContainer itemFilterContainer;
    protected int itemsLeftToTransferLastSecond;
    private CoverableItemHandlerWrapper itemHandlerWrapper;
    protected boolean isWorkingAllowed = true;

    public CoverConveyor(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                         @NotNull EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(definition, coverableView, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = itemsPerSecond;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.conveyorMode = ConveyorMode.EXPORT;
        this.distributionMode = DistributionMode.INSERT_FIRST;
        this.itemFilterContainer = new ItemFilterContainer(this);
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = MathHelper.clamp(transferRate, 1, maxItemTransferRate);
        CoverableView coverable = getCoverableView();
        coverable.markDirty();

        if (getWorld() != null && getWorld().isRemote) {
            // tile at cover holder pos
            TileEntity te = getTileEntityHere();
            if (te instanceof TileEntityItemPipe) {
                ((TileEntityItemPipe) te).resetTransferred();
            }
            // tile neighbour to holder pos at attached side
            te = getNeighbor(getAttachedSide());
            if (te instanceof TileEntityItemPipe) {
                ((TileEntityItemPipe) te).resetTransferred();
            }
        }
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
        if (timer % 5 == 0 && isWorkingAllowed && itemsLeftToTransferLastSecond > 0) {
            EnumFacing side = getAttachedSide();
            TileEntity tileEntity = coverable.getNeighbor(side);
            IItemHandler itemHandler = tileEntity == null ? null :
                    tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
            IItemHandler myItemHandler = coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (itemHandler != null && myItemHandler != null) {
                int totalTransferred = doTransferItems(itemHandler, myItemHandler, itemsLeftToTransferLastSecond);
                this.itemsLeftToTransferLastSecond -= totalTransferred;
            }
        }
        if (timer % 20 == 0) {
            this.itemsLeftToTransferLastSecond = transferRate;
        }
    }

    protected int doTransferItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
    }

    protected int doTransferItemsAny(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return moveInventoryItems(itemHandler, myItemHandler, maxTransferAmount);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return moveInventoryItems(myItemHandler, itemHandler, maxTransferAmount);
        }
        return 0;
    }

    protected int doTransferItemsByGroup(IItemHandler itemHandler, IItemHandler myItemHandler,
                                         Map<Object, GroupItemInfo> itemInfos, int maxTransferAmount) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return moveInventoryItems(itemHandler, myItemHandler, itemInfos, maxTransferAmount);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return moveInventoryItems(myItemHandler, itemHandler, itemInfos, maxTransferAmount);
        }
        return 0;
    }

    protected Map<Object, GroupItemInfo> doCountDestinationInventoryItemsByMatchIndex(IItemHandler itemHandler,
                                                                                      IItemHandler myItemHandler) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return countInventoryItemsByMatchSlot(myItemHandler);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return countInventoryItemsByMatchSlot(itemHandler);
        }
        return Collections.emptyMap();
    }

    protected Map<ItemStack, TypeItemInfo> doCountSourceInventoryItemsByType(IItemHandler itemHandler,
                                                                             IItemHandler myItemHandler) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return countInventoryItemsByType(itemHandler);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return countInventoryItemsByType(myItemHandler);
        }
        return Collections.emptyMap();
    }

    protected boolean doTransferItemsExact(IItemHandler itemHandler, IItemHandler myItemHandler,
                                           TypeItemInfo itemInfo) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return moveInventoryItemsExact(itemHandler, myItemHandler, itemInfo);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return moveInventoryItemsExact(myItemHandler, itemHandler, itemInfo);
        }
        return false;
    }

    protected static boolean moveInventoryItemsExact(IItemHandler sourceInventory, IItemHandler targetInventory,
                                                     TypeItemInfo itemInfo) {
        // first, compute how much can we extract in reality from the machine,
        // because totalCount is based on what getStackInSlot returns, which may differ from what
        // extractItem() will return
        ItemStack resultStack = itemInfo.itemStack.copy();
        int totalExtractedCount = 0;
        int itemsLeftToExtract = itemInfo.totalCount;

        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.get(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, true);
            if (!extractedStack.isEmpty() &&
                    ItemStack.areItemsEqual(resultStack, extractedStack) &&
                    ItemStack.areItemStackTagsEqual(resultStack, extractedStack)) {
                totalExtractedCount += extractedStack.getCount();
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        // if amount of items extracted is not equal to the amount of items we
        // wanted to extract, abort item extraction
        if (totalExtractedCount != itemInfo.totalCount) {
            return false;
        }
        // adjust size of the result stack accordingly
        resultStack.setCount(totalExtractedCount);

        // now, see how much we can insert into destination inventory
        // if we can't insert as much as itemInfo requires, and remainder is empty, abort, abort
        ItemStack remainder = GTTransferUtils.insertItem(targetInventory, resultStack, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        // otherwise, perform real insertion and then remove items from the source inventory
        GTTransferUtils.insertItem(targetInventory, resultStack, false);

        // perform real extraction of the items from the source inventory now
        itemsLeftToExtract = itemInfo.totalCount;
        for (int i = 0; i < itemInfo.slots.size(); i++) {
            int slotIndex = itemInfo.slots.get(i);
            ItemStack extractedStack = sourceInventory.extractItem(slotIndex, itemsLeftToExtract, false);
            if (!extractedStack.isEmpty() &&
                    ItemStack.areItemsEqual(resultStack, extractedStack) &&
                    ItemStack.areItemStackTagsEqual(resultStack, extractedStack)) {
                itemsLeftToExtract -= extractedStack.getCount();
            }
            if (itemsLeftToExtract == 0) {
                break;
            }
        }
        return true;
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     Map<Object, GroupItemInfo> itemInfos, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            AtomicBoolean matchResult = new AtomicBoolean(true);
            AtomicInteger matchSlotIndex = new AtomicInteger(-1);
            itemFilterContainer.onMatch(itemStack, (matched, match, matchedSlot) -> {
                matchResult.set(matched);
                matchSlotIndex.set(matchedSlot);
            });
            if (!matchResult.get() || !itemInfos.containsKey(matchSlotIndex.get())) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(matchSlotIndex);

            ItemStack extractedStack = sourceInventory.extractItem(i,
                    Math.min(itemInfo.totalCount, itemsLeftToTransfer), true);

            ItemStack remainderStack = GTTransferUtils.insertItem(targetInventory, extractedStack, true);
            int amountToInsert = extractedStack.getCount() - remainderStack.getCount();

            if (amountToInsert > 0) {
                extractedStack = sourceInventory.extractItem(i, amountToInsert, false);

                if (!extractedStack.isEmpty()) {

                    GTTransferUtils.insertItem(targetInventory, extractedStack, false);
                    itemsLeftToTransfer -= extractedStack.getCount();
                    itemInfo.totalCount -= extractedStack.getCount();

                    if (itemInfo.totalCount == 0) {
                        itemInfos.remove(matchResult);
                        if (itemInfos.isEmpty()) {
                            break;
                        }
                    }
                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            AtomicBoolean didMatch = new AtomicBoolean(true);
            itemFilterContainer.onMatch(sourceStack, (matched, match, matchedSlot) -> {
                didMatch.set(matched);
            });
            if (!didMatch.get()) continue;

            ItemStack remainder = GTTransferUtils.insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    GTTransferUtils.insertItem(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return maxTransferAmount - itemsLeftToTransfer;
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

    protected static class GroupItemInfo {

        public final int filterSlot;
        public final Set<ItemStack> itemStackTypes;
        public int totalCount;

        public GroupItemInfo(int filterSlot, Set<ItemStack> itemStackTypes, int totalCount) {
            this.filterSlot = filterSlot;
            this.itemStackTypes = itemStackTypes;
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

            int finalSrcIndex = srcIndex;
            itemFilterContainer.onMatch(itemStack, (matched, match, matchedSlot) -> {
                if (!matched) return;
                if (!result.containsKey(itemStack)) {
                    TypeItemInfo itemInfo = new TypeItemInfo(itemStack.copy(), matchedSlot, new IntArrayList(), 0);
                    itemInfo.totalCount += itemStack.getCount();
                    itemInfo.slots.add(finalSrcIndex);
                    result.put(itemStack.copy(), itemInfo);
                } else {
                    TypeItemInfo itemInfo = result.get(itemStack);
                    itemInfo.totalCount += itemStack.getCount();
                    itemInfo.slots.add(finalSrcIndex);
                }
            });
        }
        return result;
    }

    @NotNull
    protected Map<Object, GroupItemInfo> countInventoryItemsByMatchSlot(@NotNull IItemHandler inventory) {
        Map<Object, GroupItemInfo> result = new Object2ObjectOpenHashMap<>();
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            itemFilterContainer.onMatch(itemStack, (matched, match, matchedSlot) -> {
                if (!matched) return;
                if (!result.containsKey(matchedSlot)) {
                    GroupItemInfo itemInfo = new GroupItemInfo(matchedSlot,
                            new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount()), 0);
                    itemInfo.itemStackTypes.add(itemStack.copy());
                    itemInfo.totalCount += itemStack.getCount();
                    result.put(matchedSlot, itemInfo);
                } else {
                    GroupItemInfo itemInfo = result.get(matchedSlot);
                    itemInfo.itemStackTypes.add(itemStack.copy());
                    itemInfo.totalCount += itemStack.getCount();
                }
            });

        }
        return result;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public boolean canInteractWithOutputSide() {
        return true;
    }

    @Override
    public void onRemoval() {
        dropInventoryContents(itemFilterContainer.getFilterInventory());
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
                                                        @NotNull CuboidRayTraceResult hitResult) {
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

    protected String getUITitle() {
        return "cover.conveyor.title";
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, GuiSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176,192);

        if (getItemFilterContainer().hasItemFilter()) {
            getItemFilterContainer().setFilterStackSizer(this::getMaxStackSize);
        }

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createUI(panel, guiSyncManager))
                .bindPlayerInventory();
    }

    protected ParentWidget<Column> createUI(ModularPanel mainPanel, GuiSyncManager guiSyncManager) {
        EnumSyncValue<ManualImportExportMode> manualIOmode = new EnumSyncValue<>(ManualImportExportMode.class,
                this::getManualImportExportMode, this::setManualImportExportMode);

        EnumSyncValue<ConveyorMode> conveyorMode = new EnumSyncValue<>(ConveyorMode.class,
                this::getConveyorMode, this::setConveyorMode);

        IntSyncValue throughput = new IntSyncValue(this::getTransferRate, this::setTransferRate);

        guiSyncManager.syncValue("manual_io", manualIOmode);
        guiSyncManager.syncValue("conveyor_mode", conveyorMode);

        return new Column().top(24).margin(7, 0)
                .widthRel(1f).coverChildrenHeight()
                .child(new Row().coverChildrenHeight()
                        .marginBottom(2).widthRel(1f)
                        .child(new ButtonWidget<>()
                                .left(0).width(18)
                                .onMousePressed(mouseButton -> {
                                    throughput.updateCacheFromSource(false);
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
                                .value(throughput)
                                .background(GTGuiTextures.DISPLAY)
                                .onUpdateListener(w -> {
                                    if (throughput.updateCacheFromSource(false)) {
                                        w.setText(throughput.getStringValue());
                                    }
                                }))
                        .child(new ButtonWidget<>()
                                .right(0).width(18)
                                .onMousePressed(mouseButton -> {
                                    throughput.updateCacheFromSource(false);
                                    int val = throughput.getValue() + getIncrementValue(MouseData.create(mouseButton));
                                    throughput.setValue(val, true, true);
                                    Interactable.playButtonClickSound();
                                    return true;
                                })
                                .onUpdateListener(w -> w.overlay(createAdjustOverlay(true)))))
                .child(getItemFilterContainer()
                        .initUI(mainPanel, guiSyncManager))
                .child(new Row().coverChildrenHeight()
                        .marginBottom(2).widthRel(1f)
                        .child(createManualIoButton(manualIOmode, ManualImportExportMode.DISABLED))
                        .child(createManualIoButton(manualIOmode, ManualImportExportMode.UNFILTERED))
                        .child(createManualIoButton(manualIOmode, ManualImportExportMode.FILTERED))
                        .child(IKey.lang("Manual IO Mode")
                                .asWidget()
                                .align(Alignment.CenterRight)
                                .height(18)))
                .child(new Row().coverChildrenHeight()
                        .marginBottom(2).widthRel(1f)
                        .child(createConveyorModeButton(conveyorMode, ConveyorMode.IMPORT))
                        .child(createConveyorModeButton(conveyorMode, ConveyorMode.EXPORT))
                        .child(IKey.lang("Conveyor Mode")
                                .asWidget()
                                .align(Alignment.CenterRight)
                                .height(18)));
    }

    private Widget<ToggleButton> createManualIoButton(EnumSyncValue<ManualImportExportMode> value, ManualImportExportMode mode) {
        return new ToggleButton().size(18)
                .marginRight(2)
                .value(boolValueOf(value, mode))
                .background(GTGuiTextures.MC_BUTTON_DISABLED)
                .selectedBackground(GTGuiTextures.MC_BUTTON)
                .overlay(GTGuiTextures.MANUAL_IO_OVERLAY[mode.ordinal()])
                .addTooltipLine(switch (mode) {
                    case DISABLED -> IKey.lang("cover.universal.manual_import_export.mode.disabled");
                    case UNFILTERED -> IKey.lang("cover.universal.manual_import_export.mode.unfiltered");
                    case FILTERED -> IKey.lang("cover.universal.manual_import_export.mode.filtered");
                });
    }

    private Widget<ToggleButton> createConveyorModeButton(EnumSyncValue<ConveyorMode> value, ConveyorMode mode) {
        return new ToggleButton().size(18)
                .marginRight(2)
                .value(boolValueOf(value, mode))
                .background(GTGuiTextures.MC_BUTTON_DISABLED)
                .selectedBackground(GTGuiTextures.MC_BUTTON)
                .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY[mode.ordinal()])
                .addTooltipLine(switch (mode) {
                    case EXPORT -> IKey.lang("cover.conveyor.mode.export");
                    case IMPORT -> IKey.lang("cover.conveyor.mode.import");
                });
    }

    protected int getIncrementValue(MouseData data) {
        int adjust = 1;
        if (data.shift) adjust *= 4;
        if (data.ctrl) adjust *= 16;
        if (data.alt) adjust *= 64;
        return adjust;
    }

    protected IKey createAdjustOverlay(boolean increment) {
        final StringBuilder builder = new StringBuilder();
        builder.append(increment ? '+' : '-');
        builder.append(getIncrementValue(MouseData.create(-1)));

        float scale = 1f;
        if (builder.length() == 3) {
            scale = 0.8f;
        } else if (builder.length() == 4) {
            scale = 0.6f;
        } else if (builder.length() > 4) {
            scale = 0.5f;
        }
        return IKey.str(builder.toString())
                .scale(scale);
    }

    protected int getMaxStackSize() {
        return 1;
    }

    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        return builder.build(this, player);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, getUITitle(), GTValues.VN[tier]));

        primaryGroup.addWidget(new IncrementButtonWidget(136, 20, 30, 20, 1, 8, 64, 512, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        primaryGroup.addWidget(new IncrementButtonWidget(10, 20, 30, 20, -1, -8, -64, -512, this::adjustTransferRate)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        primaryGroup.addWidget(new ImageWidget(40, 20, 96, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new TextFieldWidget2(42, 26, 92, 20, () -> String.valueOf(transferRate), val -> {
            if (val != null && !val.isEmpty())
                setTransferRate(MathHelper.clamp(Integer.parseInt(val), 1, maxItemTransferRate));
        })
                .setNumbersOnly(1, maxItemTransferRate)
                .setMaxLength(4)
                .setPostFix("cover.conveyor.transfer_rate"));

        primaryGroup.addWidget(new gregtech.api.gui.widgets.CycleButtonWidget(10, 45, 75, 20,
                ConveyorMode.class, this::getConveyorMode, this::setConveyorMode));
        primaryGroup.addWidget(new gregtech.api.gui.widgets.CycleButtonWidget(7, 166, 116, 20,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                        .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        if (getTileEntityHere() instanceof TileEntityItemPipe ||
                getNeighbor(getAttachedSide()) instanceof TileEntityItemPipe) {
            final ImageCycleButtonWidget distributionModeButton = new ImageCycleButtonWidget(149, 166, 20, 20,
                    GuiTextures.DISTRIBUTION_MODE, 3,
                    () -> distributionMode.ordinal(),
                    val -> setDistributionMode(DistributionMode.values()[val]))
                            .setTooltipHoverString(val -> DistributionMode.values()[val].getName());
            primaryGroup.addWidget(distributionModeButton);
        }

        this.itemFilterContainer.initUI(70, primaryGroup::addWidget);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 190 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190);
        return buildUI(builder, player);
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
        packetBuffer.writeEnumValue(conveyorMode);
        packetBuffer.writeEnumValue(distributionMode);
        getItemFilterContainer().writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferRate = packetBuffer.readInt();
        this.conveyorMode = packetBuffer.readEnumValue(ConveyorMode.class);
        this.distributionMode = packetBuffer.readEnumValue(DistributionMode.class);
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
        this.conveyorMode = ConveyorMode.values()[tagCompound.getInteger("ConveyorMode")];
        this.distributionMode = DistributionMode.values()[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.values()[tagCompound.getInteger("ManualImportExportMode")];
        this.itemFilterContainer.deserializeNBT(tagCompound.getCompoundTag("Filter"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum ConveyorMode implements IStringSerializable, IIOMode {

        IMPORT("cover.conveyor.mode.import"),
        EXPORT("cover.conveyor.mode.export");

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
                    !itemFilterContainer.testItemStack(stack)) {
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
                if (result.isEmpty() || !itemFilterContainer.testItemStack(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
