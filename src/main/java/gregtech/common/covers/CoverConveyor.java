package gregtech.common.covers;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ITranslatable;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.mui.widget.GTTextFieldWidget;
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
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CoverConveyor extends CoverBase implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxItemTransferRate;
    private int transferRate;
    protected IOMode ioMode = IOMode.EXPORT;
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

    public void setIOMode(IOMode ioMode) {
        this.ioMode = ioMode;
        writeCustomData(GregtechDataCodes.UPDATE_COVER_MODE, buf -> buf.writeEnumValue(ioMode));
        markDirty();
    }

    public IOMode getIOMode() {
        return ioMode;
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
        return switch (ioMode) {
            case IMPORT -> moveInventoryItems(itemHandler, myItemHandler, maxTransferAmount);
            case EXPORT -> moveInventoryItems(myItemHandler, itemHandler, maxTransferAmount);
        };
    }

    protected int doTransferItemsByGroup(IItemHandler itemHandler, IItemHandler myItemHandler,
                                         Map<Integer, GroupItemInfo> itemInfos, int maxTransferAmount) {
        return switch (ioMode) {
            case IMPORT -> moveInventoryItems(itemHandler, myItemHandler, itemInfos, maxTransferAmount);
            case EXPORT -> moveInventoryItems(myItemHandler, itemHandler, itemInfos, maxTransferAmount);
        };
    }

    protected Map<Integer, GroupItemInfo> doCountDestinationInventoryItemsByMatchIndex(IItemHandler itemHandler,
                                                                                       IItemHandler myItemHandler) {
        return switch (ioMode) {
            case IMPORT -> countInventoryItemsByMatchSlot(myItemHandler);
            case EXPORT -> countInventoryItemsByMatchSlot(itemHandler);
        };
    }

    protected Map<ItemStack, TypeItemInfo> doCountSourceInventoryItemsByType(IItemHandler itemHandler,
                                                                             IItemHandler myItemHandler) {
        return switch (ioMode) {
            case IMPORT -> countInventoryItemsByType(itemHandler);
            case EXPORT -> countInventoryItemsByType(myItemHandler);
        };
    }

    protected boolean doTransferItemsExact(IItemHandler itemHandler, IItemHandler myItemHandler,
                                           TypeItemInfo itemInfo) {
        return switch (ioMode) {
            case IMPORT -> moveInventoryItemsExact(itemHandler, myItemHandler, itemInfo);
            case EXPORT -> moveInventoryItemsExact(myItemHandler, itemHandler, itemInfo);
        };
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
                                     Map<Integer, GroupItemInfo> itemInfos, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty()) {
                continue;
            }

            var matchResult = itemFilterContainer.match(itemStack);
            int matchSlotIndex = matchResult.getFilterIndex();
            if (!matchResult.isMatched() || !itemInfos.containsKey(matchSlotIndex)) {
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
                        itemInfos.remove(matchSlotIndex);
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

            var result = itemFilterContainer.match(sourceStack);
            if (!result.isMatched()) continue;

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

    @NotNull
    protected Map<Integer, GroupItemInfo> countInventoryItemsByMatchSlot(@NotNull IItemHandler inventory) {
        Map<Integer, GroupItemInfo> result = new Int2ObjectOpenHashMap<>();
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            var matchResult = itemFilterContainer.match(itemStack);
            if (!matchResult.isMatched()) continue;
            int matchedSlot = matchResult.getFilterIndex();

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
        dropInventoryContents(itemFilterContainer);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline,
                            @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {
        if (ioMode.isExport()) {
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
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != delegate) {
                this.itemHandlerWrapper = new CoverableItemHandlerWrapper(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandlerWrapper);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }

        return defaultValue;
    }

    @Override
    public ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        ModularPanel panel = GTGuis.createPanel(this, 176, 192 + 18);

        getItemFilterContainer().setMaxTransferSize(getMaxStackSize());

        return panel.child(CoverWithUI.createTitleRow(getPickItem()))
                .child(createUI(guiData, guiSyncManager))
                .bindPlayerInventory();
    }

    protected Flow createUI(GuiData data, PanelSyncManager guiSyncManager) {
        // noinspection DuplicatedCode
        EnumSyncValue<ManualImportExportMode> manualIOModeSync = new EnumSyncValue<>(ManualImportExportMode.class,
                this::getManualImportExportMode, this::setManualImportExportMode);
        EnumSyncValue<IOMode> conveyorModeSync = new EnumSyncValue<>(IOMode.class, this::getIOMode, this::setIOMode);
        EnumSyncValue<DistributionMode> distributionModeSync = new EnumSyncValue<>(DistributionMode.class,
                this::getDistributionMode, this::setDistributionMode);
        IntSyncValue throughputSync = new IntSyncValue(this::getTransferRate, this::setTransferRate);

        guiSyncManager.syncValue("manual_io", manualIOModeSync);
        guiSyncManager.syncValue("conveyor_mode", conveyorModeSync);
        guiSyncManager.syncValue("distribution_mode", distributionModeSync);

        Flow column = Flow.column()
                .top(24)
                .widthRel(1f)
                .margin(7, 0)
                .coverChildrenHeight();

        if (createThroughputRow()) {
            column.child(Flow.row()
                    .widthRel(1f)
                    .coverChildrenHeight()
                    .marginBottom(2)
                    .child(new ButtonWidget<>()
                            .width(18)
                            .left(0)
                            .onMousePressed(mouseButton -> {
                                int val = throughputSync.getValue() - getIncrementValue(MouseData.create(mouseButton));
                                throughputSync.setValue(Math.max(val, 1), true, true);
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(false))))
                    .child(new GTTextFieldWidget()
                            .left(18)
                            .right(18)
                            .setPostFix(" items/s")
                            .setTextColor(Color.WHITE.darker(1))
                            .setNumbers(1, maxItemTransferRate)
                            .value(throughputSync)
                            .background(GTGuiTextures.DISPLAY))
                    .child(new ButtonWidget<>()
                            .right(0)
                            .width(18)
                            .onMousePressed(mouseButton -> {
                                int val = throughputSync.getValue() + getIncrementValue(MouseData.create(mouseButton));
                                throughputSync.setValue(Math.min(val, maxItemTransferRate), true, true);
                                return true;
                            })
                            .onUpdateListener(w -> w.overlay(createAdjustOverlay(true)))));
        }

        if (createFilterRow()) {
            column.child(getItemFilterContainer().initUI(data, guiSyncManager));
        }

        if (createManualIOModeRow()) {
            // noinspection DuplicatedCode
            column.child(new EnumRowBuilder<>(ManualImportExportMode.class)
                    .value(manualIOModeSync)
                    .rowDescription(IKey.lang("cover.generic.manual_io"))
                    .overlay(new IDrawable[] {
                            new DynamicDrawable(() -> conveyorModeSync.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[0] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[0]),
                            new DynamicDrawable(() -> conveyorModeSync.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[1] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[1]),
                            new DynamicDrawable(() -> conveyorModeSync.getValue().isImport() ?
                                    GTGuiTextures.MANUAL_IO_OVERLAY_OUT[2] : GTGuiTextures.MANUAL_IO_OVERLAY_IN[2])
                    })
                    .widgetExtras((manualImportExportMode, toggleButton) -> manualImportExportMode
                            .handleTooltip(toggleButton, "conveyor"))
                    .build());
        }

        if (createConveyorModeRow()) {
            column.child(new EnumRowBuilder<>(IOMode.class)
                    .value(conveyorModeSync)
                    .rowDescription(IKey.lang("cover.generic.io"))
                    .overlay(GTGuiTextures.CONVEYOR_MODE_OVERLAY)
                    .widgetExtras((ioMode, toggleButton) -> ioMode.handleTooltip(toggleButton, "conveyor"))
                    .build());
        }

        if (createDistributionModeRow()) {
            column.child(new EnumRowBuilder<>(DistributionMode.class)
                    .value(distributionModeSync)
                    .rowDescription(IKey.lang("cover.conveyor.distribution.name"))
                    .overlay(16, GTGuiTextures.DISTRIBUTION_MODE_OVERLAY)
                    .widgetExtras(ITranslatable::handleTooltip)
                    .build());
        }

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
            this.ioMode = buf.readEnumValue(IOMode.class);
            getCoverableView().scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeInt(transferRate);
        packetBuffer.writeByte(ioMode.ordinal());
        packetBuffer.writeByte(distributionMode.ordinal());
        packetBuffer.writeByte(manualImportExportMode.ordinal());
        getItemFilterContainer().writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.transferRate = packetBuffer.readInt();
        this.ioMode = IOMode.VALUES[packetBuffer.readByte()];
        this.distributionMode = DistributionMode.VALUES[packetBuffer.readByte()];
        this.manualImportExportMode = ManualImportExportMode.VALUES[packetBuffer.readByte()];
        getItemFilterContainer().readInitialSyncData(packetBuffer);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("ConveyorMode", ioMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", this.itemFilterContainer.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.ioMode = IOMode.VALUES[tagCompound.getInteger("ConveyorMode")];
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
    @SideOnly(Side.CLIENT)
    protected @NotNull TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    private class CoverableItemHandlerWrapper extends ItemHandlerDelegate {

        public CoverableItemHandlerWrapper(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            boolean block = ioMode.isExport() && manualImportExportMode.isDisabled();
            block |= manualImportExportMode.isFiltered() && !itemFilterContainer.test(stack);

            return block ? stack : super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (ioMode.isImport() && manualImportExportMode.isDisabled()) {
                return ItemStack.EMPTY;
            } else if (manualImportExportMode.isFiltered()) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !itemFilterContainer.test(result)) {
                    return ItemStack.EMPTY;
                }

                return simulate ? result : super.extractItem(slot, amount, false);
            }

            return super.extractItem(slot, amount, simulate);
        }
    }
}
