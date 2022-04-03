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
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.ItemContainerSwitchShim;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.FluidFilterContainer;
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
// import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
// import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.UUID;

public class CoverEnderItemLink extends CoverBehavior implements CoverWithUI, ITickable, IControllable{

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

    public CoverEnderItemLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        conveyorMode = CoverConveyor.ConveyorMode.IMPORT;
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
        // this.linkedContainer = new ItemContainer
    }

    private String makeContainerName() {
        return "EILink#" + Integer.toHexString(this.color).toUpperCase();
    }

    private UUID getContainerUUID() {
        return isPrivate ? playerUUID : null;
    }

    @Override
    public boolean isWorkingEnabled() {
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {

    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ENDER_ITEM_LINK.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
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
        widgetGroup.addWidget(new SlotWidget(this.linkedContainer,0, 18, 18)
                .setBackgroundTexture(GuiTextures.FLUID_SLOT));
        widgetGroup.addWidget(new ImageWidget(147, 19, 16, 16)
                .setImage(GuiTextures.INFO_ICON)
                .setPredicate(() -> isColorTemp)
                .setTooltip("cover.ender_item_link.incomplete_hex")
                .setIgnoreColor(true));
        widgetGroup.addWidget(new CycleButtonWidget(10, 42, 75, 18,
                CoverConveyor.ConveyorMode.class, this::getConveryMode, this::setConveyorMode));
        widgetGroup.addWidget(new CycleButtonWidget(92, 42, 75, 18,
                this::isIoEnabled, this::setIoEnabled, "cover.ender_item_link.iomode.disabled", "cover.ender_item_link.iomode.enabled"));
        this.itemFilter.initUI(65, widgetGroup::addWidget);
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 221)
                .widget(widgetGroup)
                .bindPlayerInventory(player.inventory, 139)
                .build(this, player);
    }

    @Override
    public void update() {

    }

    public void setConveyorMode(CoverConveyor.ConveyorMode mode) {
        conveyorMode = mode;
        coverHolder.markDirty();
    }

    public CoverConveyor.ConveyorMode getConveryMode() {
        return conveyorMode;
    }

    public void updateTankLink() {
        this.linkedContainer.changeInventory();
        this.linkedTank.changeTank(VirtualTankRegistry.getTankCreate(makeTankName(), getTankUUID()));
        coverHolder.markDirty();
    }

    private boolean isPrivate() {
        return isPrivate;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateTankLink();
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
            updateTankLink();
        } else {
            tempColorStr = str;
            isColorTemp = true;
        }
    }

    private String getColorStr() {
        return isColorTemp ? tempColorStr : Integer.toHexString(this.color).toUpperCase();
    }

    private boolean isIoEnabled() {
        return ioEnabled;
    }

    private void setIoEnabled(boolean ioEnabled) {
        this.ioEnabled = ioEnabled;
    }
}
