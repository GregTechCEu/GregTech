package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemStackKey;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.newFilter.item.ItemFilter;
import gregtech.common.covers.newFilter.item.ItemFilterHolder;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
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

    public int getTransferRate() {
        return transferRate;
    }

    protected void setTransferRate(int transferRate) {
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

    protected void adjustTransferRate(int amount) {
        setTransferRate(MathHelper.clamp(transferRate + amount, 1, maxItemTransferRate));
    }

    protected void setConveyorMode(ConveyorMode conveyorMode) {
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
    public ItemFilter getFilter() {
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

    protected Map<ItemStackKey, TypeItemInfo> doCountSourceInventoryItemsByType(IItemHandler itemHandler, IItemHandler myItemHandler) {
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

    protected boolean moveInventoryItemsExact(IItemHandler sourceInventory, IItemHandler targetInventory, TypeItemInfo itemInfo) {
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
        public final TIntList slots;
        public int totalCount;

        public TypeItemInfo(ItemStack itemStack, Object filterSlot, TIntList slots, int totalCount) {
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

    protected Map<ItemStackKey, TypeItemInfo> countInventoryItemsByType(IItemHandler inventory) {
        Map<ItemStackKey, TypeItemInfo> result = new HashMap<>();
        for (int srcIndex = 0; srcIndex < inventory.getSlots(); srcIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(srcIndex);
            if (itemStack.isEmpty()) {
                continue;
            }
            Object transferSlotIndex = filterHolder.matchItemStack(itemStack);
            if (transferSlotIndex == null) {
                continue;
            }
            ItemStackKey itemStackKey = new ItemStackKey(itemStack);
            TypeItemInfo itemInfo = result.computeIfAbsent(itemStackKey, key -> new TypeItemInfo(itemStack.copy(), transferSlotIndex, new TIntArrayList(), 0));
            itemInfo.slots.add(srcIndex);
            itemInfo.totalCount += itemStack.getCount();
        }
        return result;
    }

    protected Map<Object, GroupItemInfo> countInventoryItemsByMatchSlot(IItemHandler inventory) {
        HashMap<Object, GroupItemInfo> result = new HashMap<>();
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
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 212);
        builder.setBackground(GuiTextures.BACKGROUND)
                .widget(new TextWidget(new Text(getUITitle()).localise(GTValues.VN[tier]))
                        .setPos(6, 6))
                .bindPlayerInventory(buildContext.getPlayer(), new Pos2d(7, 129))
                .widget(new TextWidget(new Text("container.inventory").localise())
                        .setPos(8, 119))
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
                                .setSize(80, 20))
                        .setPos(7, 18)
                        .setSize(80, 56))
                .widget(new Column()
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(GuiFunctions.getIncrementer(-1, -8, -64, -512, this::adjustTransferRate))
                                        .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                        .setSize(12, 12))
                                .widget(new TextFieldWidget()
                                        .setGetterInt(() -> transferRate)
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
                                .addTooltip(new Text("cover.universal.manual_import_export.mode.description").localise())
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .setSize(80, 12))
                        .widget(new CycleButtonWidget()
                                .setForEnum(DistributionMode.class, this::getDistributionMode, this::setDistributionMode)
                                .setTextureGetter(GuiFunctions.enumStringTextureGetter(DistributionMode.class))
                                .setBackground(GuiTextures.BASE_BUTTON)
                                .addTooltip(new Text("cover.conveyor.distribution.description").localise())
                                .setSize(80, 20))
                        .setPos(89, 18)
                        .setSize(80, 56))
                .widget(filterHolder.createFilterUI(buildContext)
                        .setPos(7, 78));
        return builder.build();
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
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", transferRate);
        tagCompound.setInteger("ConveyorMode", conveyorMode.ordinal());
        tagCompound.setInteger("DistributionMode", distributionMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", isWorkingAllowed);
        tagCompound.setInteger("ManualImportExportMode", manualImportExportMode.ordinal());
        tagCompound.setTag("Filter", this.filterHolder.serializeNBT());
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

    public enum ConveyorMode implements IStringSerializable {
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
