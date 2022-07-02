package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.drawable.shapes.Rectangle;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.FluidSlotWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.FluidTankSwitchShim;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.filter.fluid.FluidFilterHolder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.UUID;
import java.util.regex.Pattern;

public class CoverEnderFluidLink extends CoverBehavior implements CoverWithUI, ITickable, IControllable {

    public static final int TRANSFER_RATE = 8000; // mB/t

    protected CoverPump.PumpMode pumpMode;
    private int color;
    private UUID playerUUID;
    private boolean isPrivate;
    private boolean workingEnabled = true;
    private boolean ioEnabled;
    private final FluidTankSwitchShim linkedTank;
    protected final FluidFilterHolder filterHolder;

    public CoverEnderFluidLink(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        pumpMode = CoverPump.PumpMode.IMPORT;
        ioEnabled = false;
        isPrivate = false;
        playerUUID = null;
        color = 0xFFFFFFFF;
        this.linkedTank = new FluidTankSwitchShim(VirtualTankRegistry.getTankCreate(makeTankName(), null));
        filterHolder = new FluidFilterHolder(this);
    }

    private String makeTankName() {
        return "EFLink#" + Integer.toHexString(this.color).toUpperCase();
    }

    private UUID getTankUUID() {
        return isPrivate ? playerUUID : null;
    }

    public FluidFilterContainer getFluidFilterContainer() {
        return this.fluidFilter;
    }

    public boolean isIOEnabled() {
        return this.ioEnabled;
    }

    @Override
    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.ENDER_FLUID_LINK.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
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
        MetaTileEntity.clearInventory(drops, filterHolder.getFilterInventory());
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public void update() {
        if (workingEnabled && ioEnabled) {
            transferFluids();
        }
    }

    protected void transferFluids() {
        IFluidHandler fluidHandler = coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, attachedSide);
        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            GTTransferUtils.transferFluids(fluidHandler, linkedTank, TRANSFER_RATE, filterHolder::test);
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            GTTransferUtils.transferFluids(linkedTank, fluidHandler, TRANSFER_RATE, filterHolder::test);
        }
    }

    public void setPumpMode(CoverPump.PumpMode pumpMode) {
        this.pumpMode = pumpMode;
        coverHolder.markDirty();
    }

    public CoverPump.PumpMode getPumpMode() {
        return pumpMode;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 168);
        Rectangle rectangle = new Rectangle().setColor(this.color);
        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .bindPlayerInventory(buildContext.getPlayer())
                .widget(new TextWidget(Text.localised("cover.ender_fluid_link.title"))
                        .setPos(10, 5))
                .widget(new CycleButtonWidget()
                        .setToggle(this::isPrivate, this::setPrivate)
                        .setTexture(GuiTextures.BUTTON_PUBLIC_PRIVATE)
                        .setSize(18, 18)
                        .setPos(12, 18))
                .widget(rectangle.asWidget()
                        .setSize(18, 18)
                        .setPos(35, 18))
                .widget(new TextFieldWidget()
                        .setGetter(this::getColorStr)
                        .setSetter(val -> {
                            updateColor(val);
                            rectangle.setColor(this.color);
                        })
                        .setPattern(Pattern.compile("[0-9a-fA-F]{0,8}"))
                        .setValidator(this::validate)
                        .setTextAlignment(Alignment.CenterLeft)
                        .setTextColor(Color.WHITE.normal)
                        .setBackground(GuiTextures.DISPLAY_SMALL.withFixedSize(58, 18, -2, -2))
                        .setPos(60, 20)
                        .setSize(54, 14))
                .widget(new FluidSlotWidget(this.linkedTank)
                        .setPos(123, 18))
                .widget(new TextWidget(Text.localised("Mode:"))
                        .setTextAlignment(Alignment.CenterLeft)
                        .setSize(80, 12)
                        .setPos(7, 38))
                .widget(new TextWidget(Text.localised("I/O:"))
                        .setTextAlignment(Alignment.CenterLeft)
                        .setSize(80, 12)
                        .setPos(7, 50))
                .widget(new CycleButtonWidget()
                        .setForEnum(CoverPump.PumpMode.class, this::getPumpMode, this::setPumpMode)
                        .setTextureGetter(GuiFunctions.enumStringTextureGetter(CoverPump.PumpMode.class))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(80, 12)
                        .setPos(89, 38))
                .widget(new CycleButtonWidget()
                        .setToggle(this::isIoEnabled, this::setIoEnabled)
                        .setTextureGetter(val -> Text.localised(val == 0 ? "cover.ender_fluid_link.iomode.disabled" : "cover.ender_fluid_link.iomode.enabled").color(Color.WHITE.normal).shadow())
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setSize(80, 12)
                        .setPos(89, 50))
                .widget(filterHolder.createFilterUI(buildContext)
                        .setPos(7, 62));
        return builder.build();
    }

    private String validate(String val) {
        if (val.length() == 8) return val;
        StringBuilder builder1 = new StringBuilder().append(val);
        while (builder1.length() < 6) builder1.insert(0, "0");
        while (builder1.length() < 8) builder1.insert(0, "F");
        return builder1.toString();
    }

    public void updateColor(String str) {
        this.color = Integer.parseUnsignedInt(str, 16);
    }

    public String getColorStr() {
        return validate(Integer.toHexString(this.color).toUpperCase());
    }

    public void updateTankLink() {
        this.linkedTank.changeTank(VirtualTankRegistry.getTankCreate(makeTankName(), getTankUUID()));
        coverHolder.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("Frequency", color);
        tagCompound.setInteger("PumpMode", pumpMode.ordinal());
        tagCompound.setBoolean("WorkingAllowed", workingEnabled);
        tagCompound.setBoolean("IOAllowed", ioEnabled);
        tagCompound.setBoolean("Private", isPrivate);
        tagCompound.setString("PlacedUUID", playerUUID.toString());
        tagCompound.setTag("Filter", filterHolder.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.color = tagCompound.getInteger("Frequency");
        this.pumpMode = CoverPump.PumpMode.values()[tagCompound.getInteger("PumpMode")];
        this.workingEnabled = tagCompound.getBoolean("WorkingAllowed");
        this.ioEnabled = tagCompound.getBoolean("IOAllowed");
        this.isPrivate = tagCompound.getBoolean("Private");
        this.playerUUID = UUID.fromString(tagCompound.getString("PlacedUUID"));
        this.filterHolder.deserializeNBT(tagCompound.getCompoundTag("Filter"));
        updateTankLink();
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
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(linkedTank);
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
        updateTankLink();
    }
}
