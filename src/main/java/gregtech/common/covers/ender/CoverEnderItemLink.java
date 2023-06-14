package gregtech.common.covers.ender;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.enderlink.VirtualContainerRegistry;
import gregtech.api.util.enderlink.ItemContainerSwitchShim;
import gregtech.api.util.*;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.filter.ItemFilterContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CoverEnderItemLink extends CoverEnderLinkBase implements ITickable {

    private final int TRANSFER_RATE = 64;
    protected CoverConveyor.ConveyorMode conveyorMode;
    protected final ItemFilterContainer itemFilter;
    protected int itemsLeftToTransferLastSecond;
    protected ItemContainerSwitchShim linkedShim;

    public CoverEnderItemLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        conveyorMode = CoverConveyor.ConveyorMode.IMPORT;
        this.linkedShim = new ItemContainerSwitchShim(VirtualContainerRegistry.getContainerCreate(makeName(ITEM_IDENTIFIER), null));
        itemFilter = new ItemFilterContainer(this);
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        // TODO update texture to be unique
        Textures.ENDER_ITEM_LINK.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    protected void updateLink() {
        this.linkedShim.changeInventory(VirtualContainerRegistry.getContainerCreate(makeName(ITEM_IDENTIFIER), getUUID()));
        coverHolder.markDirty();
    }

    @Override
    public void onRemoved() {
        NonNullList<ItemStack> drops = NonNullList.create();
        MetaTileEntity.clearInventory(drops, itemFilter.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void update() {
        long timer = coverHolder.getOffsetTimer();
        IItemHandler targetInventory = coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide);
        if (workingEnabled && ioEnabled && itemsLeftToTransferLastSecond > 0 && timer % 5 == 0) {
            int totalTransferred = doTransferItemsAny(targetInventory, linkedShim, itemsLeftToTransferLastSecond);
            this.itemsLeftToTransferLastSecond -= totalTransferred;
        }

        if (timer % 20 == 0){
            this.itemsLeftToTransferLastSecond = TRANSFER_RATE;
        }
    }

    protected int doTransferItemsAny(IItemHandler otherHandler, IItemHandler enderHandler, int maxTransferAmount) {
        if (conveyorMode == CoverConveyor.ConveyorMode.IMPORT) {
            return moveInventoryItems(otherHandler, enderHandler, maxTransferAmount);
        } else if (conveyorMode == CoverConveyor.ConveyorMode.EXPORT) {
            return moveInventoryItems(enderHandler, otherHandler, maxTransferAmount);
        }
        return 0;
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory, int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            if (!itemFilter.testItemStack(sourceStack)) {
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

    public void setConveyorMode(CoverConveyor.ConveyorMode mode) {
        conveyorMode = mode;
        coverHolder.markDirty();
    }

    public CoverConveyor.ConveyorMode getConveyorMode() {
        return conveyorMode;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        int ROW = 3;
        int COL = 3;

        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new LabelWidget(10, 5, "cover.ender_item_link.title"));
        widgetGroup.addWidget(new ToggleButtonWidget(12, 18, 18, 18, GuiTextures.BUTTON_PUBLIC_PRIVATE,
                this::isPrivate, this::setPrivate)
                .setTooltipText("cover.ender_item_link.private.tooltip"));
        widgetGroup.addWidget(new SyncableColorRectWidget(35, 18, 18, 18, () -> color)
                .setBorderWidth(1)
                .drawCheckerboard(4, 4));
        widgetGroup.addWidget(new TextFieldWidget(58, 13, 58, 18, true,
                this::getColorStr, this::updateColor, 8)
                .setValidator(str -> str.matches("[0-9a-fA-F]*")));
        widgetGroup.addWidget(new ImageWidget(147, 19, 16, 16)
                .setImage(GuiTextures.INFO_ICON)
                .setPredicate(() -> isColorTemp)
                .setTooltip("cover.ender_item_link.incomplete_hex")
                .setIgnoreColor(true));
        widgetGroup.addWidget(new CycleButtonWidget(10, 42, 75, 18,
                CoverConveyor.ConveyorMode.class, this::getConveyorMode, this::setConveyorMode));
        widgetGroup.addWidget(new CycleButtonWidget(92, 42, 75, 18,
                this::isIoEnabled, this::setIoEnabled, "cover.ender_item_link.iomode.disabled", "cover.ender_item_link.iomode.enabled"));
        this.itemFilter.initUI(65, widgetGroup::addWidget);
/*
        WidgetGroup containerGroup = new WidgetGroup(widgetGroup.getPosition().add(new Position(18 + 5, 0)));
        for (int i = 0; i < ROW * COL; i++) {
            containerGroup.addWidget(new SlotWidget(this.linkedShim, i, 154 + (i % COL) * 18, 10 + Math.floorDiv(i, COL) * 18, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT_DARKENED));
        }*/

        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 221 + 24)
                .widget(widgetGroup)
                // .widget(containerGroup)
                .bindPlayerInventory(player.inventory, 139 + 24)
                .build(this, player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("ConveyorMode", conveyorMode.ordinal());
        tagCompound.setTag("Filter", itemFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.conveyorMode = CoverConveyor.ConveyorMode.values()[tagCompound.getInteger("ConveyorMode")];
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        updateLink();
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((IItemHandler) linkedShim);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }
}
