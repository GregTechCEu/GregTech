package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEFluidConfigWidget;
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
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author GlodBlock
 * @Description The Input Hatch that can auto fetch fluid ME storage network.
 * @Date 2023/4/20-21:21
 */
public class MetaTileEntityMEInputHatch extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IFluidTank> {

    public final static String FLUID_BUFFER_TAG = "FluidTanks";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled;
    private ExportOnlyAETank[] aeFluidTanks;

    public MetaTileEntityMEInputHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UHV, true);
        this.workingEnabled = true;
    }

    @Override
    protected void initializeInventory() {
        this.aeFluidTanks = new ExportOnlyAETank[CONFIG_SIZE];
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            this.aeFluidTanks[i] = new ExportOnlyAETank(null, null, this);
        }
        super.initializeInventory();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                try {
                    IMEMonitor<IAEFluidStack> aeNetwork = this.getProxy().getStorage().getInventory(FLUID_NET);
                    for (ExportOnlyAETank aeTank : this.aeFluidTanks) {
                        // Try to clear the wrong fluid
                        IAEFluidStack exceedFluid = aeTank.exceedFluid();
                        if (exceedFluid != null) {
                            long total = exceedFluid.getStackSize();
                            IAEFluidStack notInserted = aeNetwork.injectItems(exceedFluid, Actionable.MODULATE, this.getActionSource());
                            if (notInserted != null && notInserted.getStackSize() > 0) {
                                aeTank.drain((int) (total - notInserted.getStackSize()), true);
                                continue;
                            } else {
                                aeTank.drain((int) total, true);
                            }
                        }
                        // Fill it
                        IAEFluidStack reqFluid = aeTank.requestFluid();
                        if (reqFluid != null) {
                            IAEFluidStack extracted = aeNetwork.extractItems(reqFluid, Actionable.MODULATE, this.getActionSource());
                            if (extracted != null) {
                                aeTank.addFluid(extracted);
                            }
                        }
                    }
                } catch (GridAccessException ignore) {
                }
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
        builder.widget(new AEFluidConfigWidget(16, 25, this.aeFluidTanks));

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
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            ExportOnlyAETank tank = this.aeFluidTanks[i];
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
                ExportOnlyAETank tank = this.aeFluidTanks[tankTag.getInteger("slot")];
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
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
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
        list.addAll(Arrays.asList(this.aeFluidTanks));
    }

    public static class ExportOnlyAETank implements IFluidTank, INotifiableHandler, IConfigurableSlot<IAEFluidStack>, INBTSerializable<NBTTagCompound> {

        private final static String CONFIG_TAG = "config";
        private final static String STOCK_TAG = "stock";
        private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();
        private IAEFluidStack config;
        private IAEFluidStack stock;

        public ExportOnlyAETank(IAEFluidStack config, IAEFluidStack stock, MetaTileEntity mte) {
            this.config = config;
            this.stock = stock;
            this.notifiableEntities.add(mte);
        }

        public ExportOnlyAETank() {
            this.config = null;
            this.stock = null;
        }

        @Nullable
        public IAEFluidStack requestFluid() {
            if (this.config == null || (this.stock != null && !this.config.equals(this.stock))) {
                return null;
            }
            if (this.stock == null) {
                return this.config.copy();
            }
            if (this.stock.getStackSize() < this.config.getStackSize()) {
                return this.config.copy().setStackSize(this.config.getStackSize() - this.stock.getStackSize());
            }
            return null;
        }

        @Nullable
        public IAEFluidStack exceedFluid() {
            if (this.config == null && this.stock != null) {
                return this.stock.copy();
            }
            if (this.config != null && this.stock != null) {
                if (this.config.equals(this.stock) && this.config.getStackSize() < this.stock.getStackSize()) {
                    return this.stock.copy().setStackSize(this.stock.getStackSize() - this.config.getStackSize());
                }
                if (!this.config.equals(this.stock)) {
                    return this.stock.copy();
                }
            }
            return null;
        }

        public void addFluid(IAEFluidStack fluid) {
            if (this.stock == null) {
                this.stock = fluid.copy();
            } else {
                this.stock.add(fluid);
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            if (this.config != null) {
                NBTTagCompound configTag = new NBTTagCompound();
                this.config.writeToNBT(configTag);
                tag.setTag(CONFIG_TAG, configTag);
            }
            if (this.stock != null) {
                NBTTagCompound stockTag = new NBTTagCompound();
                this.stock.writeToNBT(stockTag);
                tag.setTag(STOCK_TAG, stockTag);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(CONFIG_TAG)) {
                this.config = AEFluidStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
            }
            if (nbt.hasKey(STOCK_TAG)) {
                this.stock = AEFluidStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
            }
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            if (this.stock != null) {
                return this.stock.getFluidStack();
            }
            return null;
        }

        @Override
        public int getFluidAmount() {
            return this.stock != null ? (int) this.stock.getStackSize() : 0;
        }

        @Override
        public int getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (this.stock == null) {
                return null;
            }
            int drained = (int) Math.min(this.stock.getStackSize(), maxDrain);
            FluidStack result = new FluidStack(this.stock.getFluid(), drained);
            if (doDrain) {
                this.stock.decStackSize(drained);
                if (this.stock.getStackSize() == 0) {
                    this.stock = null;
                }
                trigger();
            }
            return result;
        }

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.add(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.remove(metaTileEntity);
        }

        private void trigger() {
            for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
                if (metaTileEntity != null && metaTileEntity.isValid()) {
                    this.addToNotifiedList(metaTileEntity, this, true);
                }
            }
        }

        @Override
        public ExportOnlyAETank copy() {
            return new ExportOnlyAETank(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    null
            );
        }

        @Override
        public IAEFluidStack getConfig() {
            return this.config;
        }

        @Override
        public IAEFluidStack getStock() {
            return this.stock;
        }

        @Override
        public void setConfig(IAEFluidStack val) {
            this.config = val;
        }

        @Override
        public void setStock(IAEFluidStack val) {
            this.stock = val;
        }
    }

}
