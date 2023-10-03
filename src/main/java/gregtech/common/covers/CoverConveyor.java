package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.EnumSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.newgui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.filter.item.ItemFilter;
import gregtech.common.covers.filter.item.ItemFilterHolder;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class CoverConveyor extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public final int tier;
    public final int maxItemTransferRate;
    private int transferRate;
    protected ConveyorMode conveyorMode;
    protected DistributionMode distributionMode;
    protected ManualImportExportMode manualImportExportMode = ManualImportExportMode.DISABLED;
    protected final ItemFilterHolder filterHolder;
    protected int itemsLeftToTransferLastSecond;
    private CoverableItemHandlerWrapper itemHandlerWrapper;
    protected boolean isWorkingAllowed = true;

    public CoverConveyor(ICoverable coverable, EnumFacing attachedSide, int tier, int itemsPerSecond) {
        super(coverable, attachedSide);
        this.tier = tier;
        this.maxItemTransferRate = itemsPerSecond;
        this.transferRate = maxItemTransferRate;
        this.itemsLeftToTransferLastSecond = transferRate;
        this.conveyorMode = ConveyorMode.EXPORT;
        this.distributionMode = DistributionMode.INSERT_FIRST;
        this.filterHolder = new ItemFilterHolder(this);
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        coverHolder.markDirty();

        if (coverHolder.getWorld() != null && coverHolder.getWorld().isRemote) {
            // tile at cover holder pos
            TileEntity te = coverHolder.getWorld().getTileEntity(coverHolder.getPos());
            if (te instanceof TileEntityItemPipe) {
                ((TileEntityItemPipe) te).resetTransferred();
            }
            // tile neighbour to holder pos at attached side
            te = coverHolder.getWorld().getTileEntity(coverHolder.getPos().offset(attachedSide));
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
        writeUpdateData(1, buf -> buf.writeEnumValue(conveyorMode));
        coverHolder.markDirty();
    }

    public ConveyorMode getConveyorMode() {
        return conveyorMode;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public void setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
        coverHolder.markDirty();
    }

    public ManualImportExportMode getManualImportExportMode() {
        return manualImportExportMode;
    }

    protected void setManualImportExportMode(ManualImportExportMode manualImportExportMode) {
        this.manualImportExportMode = manualImportExportMode;
        coverHolder.markDirty();
    }

    public ItemFilterHolder getFilterHolder() {
        return filterHolder;
    }

    @Nullable
    public ItemFilter getItemFilter() {
        return filterHolder.getCurrentFilter();
    }

    @Override
    public void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0 && isWorkingAllowed && itemsLeftToTransferLastSecond > 0) {
            TileEntity tileEntity = coverHolder.getWorld().getTileEntity(coverHolder.getPos().offset(attachedSide));
            IItemHandler itemHandler = tileEntity == null ? null : tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide.getOpposite());
            IItemHandler myItemHandler = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide);
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

    protected int doTransferItemsByGroup(IItemHandler itemHandler, IItemHandler myItemHandler, Map<Object, GroupItemInfo> itemInfos, int maxTransferAmount) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return moveInventoryItems(itemHandler, myItemHandler, itemInfos, maxTransferAmount);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return moveInventoryItems(myItemHandler, itemHandler, itemInfos, maxTransferAmount);
        }
        return 0;
    }

    protected Map<Object, GroupItemInfo> doCountDestinationInventoryItemsByMatchIndex(IItemHandler itemHandler, IItemHandler myItemHandler) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return countInventoryItemsByMatchSlot(myItemHandler);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return countInventoryItemsByMatchSlot(itemHandler);
        }
        return Collections.emptyMap();
    }

    protected Map<ItemStack, TypeItemInfo> doCountSourceInventoryItemsByType(IItemHandler itemHandler, IItemHandler myItemHandler) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return countInventoryItemsByType(itemHandler);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return countInventoryItemsByType(myItemHandler);
        }
        return Collections.emptyMap();
    }

    protected boolean doTransferItemsExact(IItemHandler itemHandler, IItemHandler myItemHandler, TypeItemInfo itemInfo) {
        if (conveyorMode == ConveyorMode.IMPORT) {
            return moveInventoryItemsExact(itemHandler, myItemHandler, itemInfo);
        } else if (conveyorMode == ConveyorMode.EXPORT) {
            return moveInventoryItemsExact(myItemHandler, itemHandler, itemInfo);
        }
        return false;
    }

    protected static boolean moveInventoryItemsExact(IItemHandler sourceInventory, IItemHandler targetInventory, TypeItemInfo itemInfo) {
        //first, compute how much can we extract in reality from the machine,
        //because totalCount is based on what getStackInSlot returns, which may differ from what
        //extractItem() will return
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
        //if amount of items extracted is not equal to the amount of items we
        //wanted to extract, abort item extraction
        if (totalExtractedCount != itemInfo.totalCount) {
            return false;
        }
        //adjust size of the result stack accordingly
        resultStack.setCount(totalExtractedCount);

        //now, see how much we can insert into destination inventory
        //if we can't insert as much as itemInfo requires, and remainder is empty, abort, abort
        ItemStack remainder = GTTransferUtils.insertItem(targetInventory, resultStack, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        //otherwise, perform real insertion and then remove items from the source inventory
        GTTransferUtils.insertItem(targetInventory, resultStack, false);

        //perform real extraction of the items from the source inventory now
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

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory, Map<Object, GroupItemInfo> itemInfos, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int i = 0; i < sourceInventory.getSlots(); i++) {
            ItemStack itemStack = sourceInventory.getStackInSlot(i);
            if (itemStack.isEmpty()) {
                continue;
            }
            Object matchSlotIndex = filterHolder.matchItemStack(itemStack);
            if (matchSlotIndex == null || !itemInfos.containsKey(matchSlotIndex)) {
                continue;
            }

            GroupItemInfo itemInfo = itemInfos.get(matchSlotIndex);

            ItemStack extractedStack = sourceInventory.extractItem(i, Math.min(itemInfo.totalCount, itemsLeftToTransfer), true);

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

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            if (!filterHolder.test(sourceStack)) {
                continue;
            }
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
        public final Object filterSlot;
        public final IntList slots;
        public int totalCount;

        public TypeItemInfo(ItemStack itemStack, Object filterSlot, IntList slots, int totalCount) {
            this.itemStack = itemStack;
            this.filterSlot = filterSlot;
            this.slots = slots;
            this.totalCount = totalCount;
        }
    }

    protected static class GroupItemInfo {

        public final Object filterSlot;
        public int totalCount;

        public GroupItemInfo(Object filterSlot, int totalCount) {
            this.filterSlot = filterSlot;
            this.totalCount = totalCount;
        }
    }

    @Nonnull
    protected Map<ItemStack, TypeItemInfo> countInventoryItemsByType(@Nonnull IItemHandler inventory) {
        Map<ItemStack, TypeItemInfo> result = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }
            Object transferSlotIndex = filterHolder.matchItemStack(itemStack);
            if (transferSlotIndex == null) {
                continue;
            }
            TypeItemInfo itemInfo = result.computeIfAbsent(itemStack, key -> new TypeItemInfo(itemStack.copy(), transferSlotIndex, new IntArrayList(), 0));
            itemInfo.slots.add(srcIndex);
            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }

    @Nonnull
    protected Map<Object, GroupItemInfo> countInventoryItemsByMatchSlot(@Nonnull IItemHandler inventory) {
        Map<Object, GroupItemInfo> result = new Object2ObjectOpenHashMap<>();
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }
            Object transferSlotIndex = filterHolder.matchItemStack(itemStack);
            if (transferSlotIndex == null) {
                continue;
            }
            GroupItemInfo itemInfo = result.computeIfAbsent(transferSlotIndex, key -> new GroupItemInfo(transferSlotIndex, 0));
            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public boolean shouldCoverInteractWithOutputside() {
        return true;
    }

    @Override
    public void onRemoved() {
        NonNullList<ItemStack> drops = NonNullList.create();
        MetaTileEntity.clearInventory(drops, filterHolder.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        if (conveyorMode == ConveyorMode.EXPORT) {
            Textures.CONVEYOR_OVERLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        } else {
            Textures.CONVEYOR_OVERLAY_INVERTED.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
        }
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            //openUI((EntityPlayerMP) playerIn);
            GTGuis.openCoverUi(this, playerIn);
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
                        .setPostFix("cover.conveyor.transfer_rate")
        );

        primaryGroup.addWidget(new CycleButtonWidget(10, 45, 75, 20,
                ConveyorMode.class, this::getConveyorMode, this::setConveyorMode));
        primaryGroup.addWidget(new CycleButtonWidget(7, 166, 116, 20,
                ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)
                .setTooltipHoverString("cover.universal.manual_import_export.mode.description"));

        if (coverHolder.getWorld().getTileEntity(coverHolder.getPos()) instanceof TileEntityItemPipe ||
                coverHolder.getWorld().getTileEntity(coverHolder.getPos().offset(attachedSide)) instanceof TileEntityItemPipe) {
            final ImageCycleButtonWidget distributionModeButton = new ImageCycleButtonWidget(149, 166, 20, 20, GuiTextures.DISTRIBUTION_MODE, 3,
                    () -> distributionMode.ordinal(),
                    val -> setDistributionMode(DistributionMode.values()[val]))
                    .setTooltipHoverString(val -> DistributionMode.values()[val].getName());
            primaryGroup.addWidget(distributionModeButton);
        }

        //this.itemFilterContainer.initUI(70, primaryGroup::addWidget);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 190 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190);
        return buildUI(builder, player);
    }

    public IWidget getTitleWidget() {
        ItemStack item = getCoverDefinition().getDropItemStack();
        return new Row().height(16).coverChildrenWidth()
                .child(new ItemDrawable(item).asWidget().size(16).marginRight(4))
                .child(IKey.str(item.getDisplayName()).asWidget().heightRel(1f))
                .pos(7, 7);
    }

    public ParentWidget<?> settingsRow() {
        return new ParentWidget<>().height(16).widthRel(1f).marginBottom(2);
    }

    private <T extends Enum<T>> BoolValue.Dynamic boolValueOf(EnumSyncValue<T> syncValue, T value) {
        return new BoolValue.Dynamic(() -> syncValue.getValue() == value, val -> syncValue.setValue(value));
    }

    @Override
    public ModularPanel buildUI(GuiCreationContext creationContext, GuiSyncManager syncManager, boolean isClient) {
        EnumSyncValue<ConveyorMode> ioModeValue = new EnumSyncValue<>(ConveyorMode.class, this::getConveyorMode, this::setConveyorMode);
        EnumSyncValue<ManualImportExportMode> manualIoModeValue = new EnumSyncValue<>(ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode);
        EnumSyncValue<DistributionMode> distributionModeValue = new EnumSyncValue<>(DistributionMode.class, this::getDistributionMode, this::setDistributionMode);
        syncManager.syncValue("io_mode", ioModeValue);
        syncManager.syncValue("manual_io_mode", manualIoModeValue);
        syncManager.syncValue("distribution_mode", distributionModeValue);

        ModularPanel panel = GTGuis.createPanel("conveyor", 176, 202);
        IWidget filterUI = this.filterHolder.createFilterUI(panel, creationContext, syncManager);
        filterUI.flex().widthRel(1f).marginBottom(2);
        panel.child(getTitleWidget())
                .bindPlayerInventory()
                .child(new Column()
                        .widthRel(1f).margin(10, 0)
                        .top(27).bottom(7)
                        .child(settingsRow()
                                .child(new ToggleButton().size(16).left(0)
                                        .overlay(gregtech.api.newgui.GuiTextures.EXPORT)
                                        .value(boolValueOf(ioModeValue, ConveyorMode.EXPORT)))
                                .child(new ToggleButton().size(16).left(18)
                                        .overlay(gregtech.api.newgui.GuiTextures.IMPORT)
                                        .value(boolValueOf(ioModeValue, ConveyorMode.IMPORT)))
                                .child(IKey.str("Import/Export").asWidget().height(16).left(60)))
                        .child(settingsRow()
                                .child(new ToggleButton().size(16).left(0)
                                        .overlay(gregtech.api.newgui.GuiTextures.CROSS)
                                        .value(boolValueOf(manualIoModeValue, ManualImportExportMode.DISABLED)))
                                .child(new ToggleButton().size(16).left(18)
                                        .overlay(gregtech.api.newgui.GuiTextures.FILTERED)
                                        .value(boolValueOf(manualIoModeValue, ManualImportExportMode.FILTERED)))
                                .child(new ToggleButton().size(16).left(36)
                                        .overlay(gregtech.api.newgui.GuiTextures.UNFILTERED)
                                        .value(boolValueOf(manualIoModeValue, ManualImportExportMode.UNFILTERED)))
                                .child(IKey.str("Manual IO Mode").asWidget().height(16).left(60)))
                        .child(settingsRow()
                                .child(new ToggleButton().size(16).left(0)
                                        .overlay(gregtech.api.newgui.GuiTextures.FIRST_INSERT)
                                        .value(boolValueOf(distributionModeValue, DistributionMode.INSERT_FIRST)))
                                .child(new ToggleButton().size(16).left(18)
                                        .overlay(gregtech.api.newgui.GuiTextures.ROUND_ROBIN)
                                        .value(boolValueOf(distributionModeValue, DistributionMode.ROUND_ROBIN_PRIO)))
                                .child(new ToggleButton().size(16).left(36)
                                        .overlay(gregtech.api.newgui.GuiTextures.ROUND_ROBIN_GLOBAL)
                                        .value(boolValueOf(distributionModeValue, DistributionMode.ROUND_ROBIN_GLOBAL)))
                                .child(IKey.str("Distribution Mode").asWidget().height(16).left(60)))
                        .child(settingsRow().height(12)
                                .child(new TextFieldWidget()
                                        .value(new IntSyncValue(() -> transferRate, this::setTransferRate))
                                        .setNumbers(1, maxItemTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .size(56, 12))
                                .child(IKey.str("Items/tick").asWidget().height(12).left(60)))
                        .child(filterUI));
        /*panel.child(IKey.format(getUITitle(), GTValues.VN[tier]).asWidget().pos(6, 6))
                .bindPlayerInventory()
                .child(new Column()
                        .child(IKey.lang("cover.transfer_rate").asWidget()
                                .size(80, 12))
                        .child(IKey.lang("cover.mode").asWidget()
                                .size(80, 12))
                        .child(IKey.lang("cover.mode.manual_io").asWidget()
                                .size(80, 12))
                        .child(IKey.lang("cover.conveyor.distribution_mode").asWidget()
                                .size(80, 12))
                        .pos(7, 18)
                        .size(80, 48))
                .child(new Column()
                        .child(new Row()
                                .coverChildren()
                                .child(new ButtonWidget<>()
                                        .syncHandler(new InteractionSyncHandler()
                                                .setOnMousePressed(GuiFunctions.getIncrementer(-1, -8, -64, -512, this::adjustTransferRate)))
                                        .addTooltipLine(IKey.format("modularui.decrement.tooltip", 1, 8, 64, 512))
                                        .overlay(IKey.lang("-").color(0xFFFFFF))
                                        .size(12, 12))
                                .child(new TextFieldWidget()
                                        .value(new IntSyncValue(() -> transferRate, this::setTransferRate))
                                        .setNumbers(1, maxItemTransferRate)
                                        .setTextAlignment(Alignment.Center)
                                        .size(56, 12))
                                .child(new ButtonWidget<>()
                                        .syncHandler(new InteractionSyncHandler()
                                                .setOnMousePressed(GuiFunctions.getIncrementer(1, 8, 64, 512, this::adjustTransferRate)))
                                        .addTooltipLine(IKey.format("modularui.increment.tooltip", 1, 8, 64, 512))
                                        .overlay(IKey.lang("+").color(0xFFFFFF))
                                        .size(12, 12)))
                        .child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
                                .value(new EnumSyncValue<>(ConveyorMode.class, this::getConveyorMode, this::setConveyorMode))
                                .textureGetter(GuiFunctions.enumStringTextureGetter(ConveyorMode.class))
                                .size(80, 12))
                        .child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
                                .value(new EnumSyncValue<>(ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode))
                                .textureGetter(GuiFunctions.enumStringTextureGetter(ManualImportExportMode.class))
                                .addTooltip(0, IKey.lang(ManualImportExportMode.values()[0].localeDescription))
                                .addTooltip(1, IKey.lang(ManualImportExportMode.values()[1].localeDescription))
                                .addTooltip(2, IKey.lang(ManualImportExportMode.values()[2].localeDescription))
                                .size(80, 12))
                        .child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
                                .value(new EnumSyncValue<>(DistributionMode.class, this::getDistributionMode, this::setDistributionMode))
                                .textureGetter(GuiFunctions.enumStringTextureGetter(DistributionMode.class))
                                .addTooltip(0, IKey.lang(DistributionMode.values()[0].localeDescription))
                                .addTooltip(1, IKey.lang(DistributionMode.values()[1].localeDescription))
                                .addTooltip(2, IKey.lang(DistributionMode.values()[2].localeDescription))
                                .setEnabledIf(widget -> hasItemPipeNeighbour())
                                .size(80, 12))
                        .pos(89, 18)
                        .size(80, 48));*/
        //IWidget filterUI = this.filterHolder.createFilterUI(panel, creationContext, syncManager);
        //filterUI.flex().pos(7, 66);
        //panel.child(filterUI);
        return panel;
    }

    private boolean hasItemPipeNeighbour() {
        return coverHolder.getWorld().getTileEntity(coverHolder.getPos()) instanceof TileEntityItemPipe ||
                coverHolder.getWorld().getTileEntity(coverHolder.getPos().offset(attachedSide)) instanceof TileEntityItemPipe;
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
    public void readUpdateData(int id, PacketBuffer packetBuffer) {
        super.readUpdateData(id, packetBuffer);
        if (id == 1) {
            this.conveyorMode = packetBuffer.readEnumValue(ConveyorMode.class);
            coverHolder.scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeEnumValue(conveyorMode);
        packetBuffer.writeEnumValue(distributionMode);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        this.conveyorMode = packetBuffer.readEnumValue(ConveyorMode.class);
        this.distributionMode = packetBuffer.readEnumValue(DistributionMode.class);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("ConveyorMode", conveyorMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", this.filterHolder.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
        this.conveyorMode = ConveyorMode.values()[tagCompound.getInteger("ConveyorMode")];
        this.distributionMode = DistributionMode.values()[tagCompound.getInteger("DistributionMode")];
        this.isWorkingAllowed = tagCompound.getBoolean("WorkingAllowed");
        this.manualImportExportMode = ManualImportExportMode.values()[tagCompound.getInteger("ManualImportExportMode")];
        this.filterHolder.deserializeNBT(tagCompound.getCompoundTag("Filter"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[this.tier].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    public enum ConveyorMode implements IStringSerializable, IIOMode {
        IMPORT("cover.conveyor.mode.import"),
        EXPORT("cover.conveyor.mode.export");

        public final String localeName;

        ConveyorMode(String localeName) {
            this.localeName = localeName;
        }

        @Nonnull
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

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (conveyorMode == ConveyorMode.EXPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return stack;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED && !filterHolder.test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (conveyorMode == ConveyorMode.IMPORT && manualImportExportMode == ManualImportExportMode.DISABLED) {
                return ItemStack.EMPTY;
            }
            if (manualImportExportMode == ManualImportExportMode.FILTERED) {
                ItemStack result = super.extractItem(slot, amount, true);
                if (result.isEmpty() || !filterHolder.test(result)) {
                    return ItemStack.EMPTY;
                }
                return simulate ? result : super.extractItem(slot, amount, false);
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
