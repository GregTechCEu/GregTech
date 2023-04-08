package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.enderlink.VirtualContainerRegistry;
import gregtech.api.util.enderlink.ItemContainerSwitchShim;
import gregtech.api.util.*;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.ItemFilterContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.UUID;

public class CoverEnderItemLink extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    private final int TRANSFER_RATE = 64;

    protected CoverConveyor.ConveyorMode conveyorMode;
    private int color;
    private UUID playerUUID;
    private boolean isPrivate;
    private boolean workingEnabled = true;
    private boolean ioEnabled;
    private String tempColorStr;
    private boolean isColorTemp;
    private final ItemContainerSwitchShim linkedContainer;
    protected final ItemFilterContainer itemFilter;
    protected int itemsLeftToTransferLastSecond;

    public CoverEnderItemLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        conveyorMode = CoverConveyor.ConveyorMode.IMPORT;
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
        this.linkedContainer = new ItemContainerSwitchShim(VirtualContainerRegistry.getContainerCreate(makeContainerName(), null));
        itemFilter = new ItemFilterContainer(this);
    }

    private String makeContainerName() {
        return "EILink#" + Integer.toHexString(this.color).toUpperCase();
    }

    private UUID getContainerUUID() {
        return isPrivate ? playerUUID : null;
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
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onAttached(ItemStack itemStack, EntityPlayer player) {
        super.onAttached(itemStack, player);
        if (player != null) {
            this.playerUUID = player.getUniqueID();
        }
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
            int totalTransferred = doTransferItemsAny(targetInventory, linkedContainer, itemsLeftToTransferLastSecond);
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
    public void openUI(EntityPlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI(this, player);
        isColorTemp = false;
    }



    @Override
    public ModularUI createUI(EntityPlayer player) {
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

        WidgetGroup containerGroup = new WidgetGroup(new Position(widgetGroup.getPosition().getX() + 18 + 5, widgetGroup.getPosition().getY()));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                containerGroup.addWidget(new SlotWidget(this.linkedContainer, row + col, 154 + (col * 18), 10 + (row * 18), false, false).setBackgroundTexture(GuiTextures.SLOT_DARKENED));
            }
        }

        return ModularUI.builder(GuiTextures.BACKGROUND, 100 + (16 * 9), 221)
                .widget(widgetGroup)
                .widget(containerGroup)
                .bindPlayerInventory(player.inventory, 139)
                .build(this, player);
    }

    private void updateColor(String str) {
        if (str.length() == 8) {
            isColorTemp = false;
            // stupid java not having actual unsigned ints
            long tmp = Long.parseLong(str, 16);
            if (tmp > 0x7FFFFFFF) {
                tmp -= 0x100000000L;
            }
            this.color = (int) tmp;
            updateContainerLink();
        } else {
            tempColorStr = str;
            isColorTemp = true;
        }
    }

    private String getColorStr() {
        return isColorTemp ? tempColorStr : Integer.toHexString(this.color).toUpperCase();
    }

    public void updateContainerLink() {
        this.linkedContainer.changeInventory(VirtualContainerRegistry.getContainerCreate(makeContainerName(), getContainerUUID()));
        coverHolder.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", color);
        tagCompound.setInteger("PumpMode", conveyorMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", workingEnabled);
        tagCompound.setBoolean("IOAllowed", ioEnabled);
        tagCompound.setBoolean("Private", isPrivate);
        tagCompound.setString("PlacedUUID", playerUUID.toString());
        tagCompound.setTag("Filter", itemFilter.serializeNBT());

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.color = tagCompound.getInteger("Frequency");
        this.conveyorMode = CoverConveyor.ConveyorMode.values()[tagCompound.getInteger("PumpMode")];
        this.workingEnabled = tagCompound.getBoolean("WorkingAllowed");
        this.ioEnabled = tagCompound.getBoolean("IOAllowed");
        this.isPrivate = tagCompound.getBoolean("Private");
        this.playerUUID = UUID.fromString(tagCompound.getString("PlacedUUID"));
        this.itemFilter.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        updateContainerLink();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.color);
        packetBuffer.writeString(this.playerUUID == null ? "null" : this.playerUUID.toString());
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.color = packetBuffer.readInt();
        //does client even need uuid info? just in case
        String uuidStr = packetBuffer.readString(36);
        this.playerUUID = uuidStr.equals("null") ? null : UUID.fromString(uuidStr);
        //client does not need the actual tank reference, the default one will do just fine
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workingEnabled = isActivationAllowed;
    }

    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(linkedContainer);
        }
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return defaultValue;
    }

    private boolean isIoEnabled() {
        return ioEnabled;
    }

    private void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }

    private boolean isPrivate() {
        return isPrivate;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateContainerLink();
    }
}
