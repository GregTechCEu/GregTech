package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEFluidConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MetaTileEntityMEInputHatch extends MetaTileEntityAEHostablePart
                                        implements IMultiblockAbilityPart<IFluidTank> {

    public final static String FLUID_BUFFER_TAG = "FluidTanks";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled;
    private ExportOnlyAEFluidList aeFluidHandler;

    public MetaTileEntityMEInputHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UHV, false);
        this.workingEnabled = true;
    }

    protected ExportOnlyAEFluidList getAEFluidHandler() {
        if (aeFluidHandler == null) {
            aeFluidHandler = new ExportOnlyAEFluidList(this, CONFIG_SIZE, this.getController());
        }
        return aeFluidHandler;
    }

    @Override
    protected void initializeInventory() {
        getAEFluidHandler(); // initialize it
        super.initializeInventory();
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, getAEFluidHandler().getInventory());
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                syncME();
            }
        }
    }

    protected void syncME() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEFluidSlot aeTank : this.getAEFluidHandler().getInventory()) {
            // Try to clear the wrong fluid
            IAEFluidStack exceedFluid = aeTank.exceedStack();
            if (exceedFluid != null) {
                long total = exceedFluid.getStackSize();
                IAEFluidStack notInserted = monitor.injectItems(exceedFluid, Actionable.MODULATE,
                        this.getActionSource());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    aeTank.drain((int) (total - notInserted.getStackSize()), true);
                    continue;
                } else {
                    aeTank.drain((int) total, true);
                }
            }
            // Fill it
            IAEFluidStack reqFluid = aeTank.requestStack();
            if (reqFluid != null) {
                IAEFluidStack extracted = monitor.extractItems(reqFluid, Actionable.MODULATE, this.getActionSource());
                if (extracted != null) {
                    aeTank.addStack(extracted);
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        flushInventory();
        super.onRemoval();
    }

    protected void flushInventory() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEFluidSlot aeTank : this.getAEFluidHandler().getInventory()) {
            IAEFluidStack stock = aeTank.getStock();
            if (stock instanceof WrappedFluidStack) {
                stock = ((WrappedFluidStack) stock).getAEStack();
            }
            if (stock != null) {
                monitor.injectItems(stock, Actionable.MODULATE, this.getActionSource());
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputHatch(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                I18n.format("gregtech.gui.me_network.online") :
                I18n.format("gregtech.gui.me_network.offline"),
                0xFFFFFFFF);

        // Config slots
        builder.widget(new AEFluidConfigWidget(16, 25, this.getAEFluidHandler().getInventory()));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder.build(this.getHolder(), entityPlayer);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        NBTTagList tanks = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEFluidSlot tank = this.getAEFluidHandler().getInventory()[i];
            NBTTagCompound tankTag = new NBTTagCompound();
            tankTag.setInteger("slot", i);
            tankTag.setTag("tank", tank.serializeNBT());
            tanks.appendTag(tankTag);
        }
        data.setTag(FLUID_BUFFER_TAG, tanks);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(FLUID_BUFFER_TAG, 9)) {
            NBTTagList tanks = (NBTTagList) data.getTag(FLUID_BUFFER_TAG);
            for (NBTBase nbtBase : tanks) {
                NBTTagCompound tankTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEFluidSlot tank = this.getAEFluidHandler().getInventory()[tankTag.getInteger("slot")];
                tank.deserializeNBT(tankTag.getCompoundTag("tank"));
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> list) {
        list.addAll(Arrays.asList(this.getAEFluidHandler().getInventory()));
    }

    @Nullable
    protected IMEMonitor<IAEFluidStack> getMonitor() {
        AENetworkProxy proxy = getProxy();
        if (proxy == null) return null;
        try {
            return proxy.getStorage().getInventory(FLUID_NET);
        } catch (GridAccessException ignored) {
            return null;
        }
    }
}
