package gregtech.api.metatileentity;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.google.common.base.Preconditions;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.gui.IUIHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.GTNameTagParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.core.network.packets.PacketRecoverMTE;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

@InterfaceList(value = {
        @Interface(iface = "appeng.api.networking.security.IActionHost", modid = GTValues.MODID_APPENG, striprefs = true),
        @Interface(iface = "appeng.me.helpers.IGridProxyable", modid = GTValues.MODID_APPENG, striprefs = true),
})
public class MetaTileEntityHolder extends TickableTileEntityBase implements IGregTechTileEntity, IUIHolder, IWorldNameable, IActionHost, IGridProxyable {

    MetaTileEntity metaTileEntity;
    private boolean needToUpdateLightning = false;
    private String customName;
    @SideOnly(Side.CLIENT)
    private GTNameTagParticle nameTagParticle;

    public static final int TRACKED_TICKS = 20;
    private final int[] timeStatistics = new int[TRACKED_TICKS];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;
    protected static final DecimalFormat tricorderFormat = new DecimalFormat("#.#########");

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * Sets this holder's current meta tile entity to copy of given one
     * Note that this method copies given meta tile entity and returns actual instance
     * so it is safe to call it on sample meta tile entities
     * Also can use certain data to preinit the block before data is synced
     */
    @Override
    public MetaTileEntity setMetaTileEntity(MetaTileEntity sampleMetaTileEntity) {
        Preconditions.checkNotNull(sampleMetaTileEntity, "metaTileEntity");
        setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
        if (hasWorld() && !getWorld().isRemote) {
            updateBlockOpacity();
            writeCustomData(INITIALIZE_MTE, buffer -> {
                buffer.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(getMetaTileEntity().metaTileEntityId));
                getMetaTileEntity().writeInitialSyncData(buffer);
            });
            //just to update neighbours so cables and other things will work properly
            this.needToUpdateLightning = true;
            world.neighborChanged(getPos(), getBlockType(), getPos());
            markDirty();
        }
        return metaTileEntity;
    }

    protected void setRawMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.metaTileEntity.holder = this;
    }

    private void updateBlockOpacity() {
        IBlockState currentState = world.getBlockState(getPos());
        boolean isMetaTileEntityOpaque = metaTileEntity.isOpaqueCube();
        if (currentState.getValue(BlockMachine.OPAQUE) != isMetaTileEntityOpaque) {
            world.setBlockState(getPos(), currentState.withProperty(BlockMachine.OPAQUE, isMetaTileEntityOpaque));
        }
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        customName = compound.getString(GregtechDataCodes.CUSTOM_NAME);
        if (compound.hasKey("MetaId", NBT.TAG_STRING)) {
            String metaTileEntityIdRaw = compound.getString("MetaId");
            ResourceLocation metaTileEntityId = new ResourceLocation(metaTileEntityIdRaw);
            MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            NBTTagCompound metaTileEntityData = compound.getCompoundTag("MetaTileEntity");
            if (sampleMetaTileEntity != null) {
                setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
                /* Note: NBTs need to be read before onAttached is run, since NBTs may contain important information
                 * about the composition of the BlockPattern that onAttached may generate. */
                this.metaTileEntity.readFromNBT(metaTileEntityData);
            } else {
                GTLog.logger.error("Failed to load MetaTileEntity with invalid ID " + metaTileEntityIdRaw);
            }
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                readFromNBT_AENetwork(compound);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(GregtechDataCodes.CUSTOM_NAME, getName());
        if (metaTileEntity != null) {
            compound.setString("MetaId", metaTileEntity.metaTileEntityId.toString());
            NBTTagCompound metaTileEntityData = new NBTTagCompound();
            metaTileEntity.writeToNBT(metaTileEntityData);
            compound.setTag("MetaTileEntity", metaTileEntityData);
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                writeToNBT_AENetwork(compound);
            }
        }
        return compound;
    }

    @Override
    public void invalidate() {
        if (metaTileEntity != null) {
            metaTileEntity.invalidate();
        }
        super.invalidate();
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            invalidateAE();
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        Object metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null ? metaTileEntityValue : super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        long tickTime = System.nanoTime();
        if (metaTileEntity != null) {
            metaTileEntity.update();
        } else if (world.isRemote) { // recover the mte
            GregTechAPI.networkHandler.sendToServer(new PacketRecoverMTE(world.provider.getDimension(), getPos()));
        } else { // remove the block
            if (world.getBlockState(pos).getBlock() instanceof BlockMachine) {
                world.setBlockToAir(pos);
            }
        }

        if (this.needToUpdateLightning) {
            getWorld().checkLight(getPos());
            this.needToUpdateLightning = false;
        }

        if (!world.isRemote && metaTileEntity != null && getMetaTileEntity().isValid()) {
            tickTime = System.nanoTime() - tickTime;
            if (timeStatistics.length > 0) {
                timeStatistics[timeStatisticsIndex] = (int) tickTime;
                timeStatisticsIndex = (timeStatisticsIndex + 1) % timeStatistics.length;
            }
            if (tickTime > 100_000_000L && getMetaTileEntity().doTickProfileMessage() && lagWarningCount++ < 10)
                GTLog.logger.warn("WARNING: Possible Lag Source at [" + getPos().getX() + ", " + getPos().getY() + ", " + getPos().getZ() + "] in Dimension " + world.provider.getDimension() + " with " + tickTime + "ns caused by an instance of " + getMetaTileEntity().getClass());
        }

        //increment only after current tick, so meta tile entities will get first tick as timer == 0
        //and update their settings which depend on getTimer() % N properly
        super.update();
    }

    public ArrayList<ITextComponent> getDebugInfo(EntityPlayer player, int logLevel) {
        ArrayList<ITextComponent> list = new ArrayList<>();
        if (logLevel > 2) {
            if (isValid()) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getMetaTileEntity().metaTileEntityId.toString()).setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_valid").setStyle(new Style().setColor(TextFormatting.GREEN))
                ));
            } else if (metaTileEntity == null) {
                //noinspection NoTranslation
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation("-1").setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_invalid_null").setStyle(new Style().setColor(TextFormatting.RED))
                ));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getMetaTileEntity().metaTileEntityId.toString()).setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_invalid").setStyle(new Style().setColor(TextFormatting.RED))
                ));
            }
        }
        if (logLevel > 1) {
            double[] timeStats = getTimeStatistics();
            if (timeStats != null) {
                double averageTickTime = timeStats[0];
                double worstTickTime = timeStats[1];

                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load",
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(averageTickTime / timeStatistics.length)).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(timeStatistics.length)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(worstTickTime)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load_seconds", tricorderFormat.format(worstTickTime / 1000000000)));
            }

            if (lagWarningCount > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_lag_count",
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(lagWarningCount)).setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(100_000_000L)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
            }
        }
        return list;
    }

    /**
     * @return double array of length 2, with index 0 being the average time and index 1 the worst time, in ns.
     *         If there is no tick time, it will return null.
     */
    public double[] getTimeStatistics() {
        if (timeStatistics.length > 0) {
            double averageTickTime = 0;
            double worstTickTime = 0;
            for (int tickTime : timeStatistics) {
                averageTickTime += tickTime;
                if (tickTime > worstTickTime) {
                    worstTickTime = tickTime;
                }
            }
            return new double[] { averageTickTime, worstTickTime };
        }
        return null;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeString(getName());
        if (metaTileEntity != null) {
            buf.writeBoolean(true);
            buf.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncData(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        setCustomName(buf.readString(Short.MAX_VALUE));
        if (buf.readBoolean()) {
            receiveMTEInitializationData(buf);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buffer) {
        if (discriminator == INITIALIZE_MTE) {
            receiveMTEInitializationData(buffer);
        } else if (metaTileEntity != null) {
            metaTileEntity.receiveCustomData(discriminator, buffer);
        }
    }

    /**
     * Sets and initializes the MTE
     *
     * @param buf the buffer to read data from
     */
    private void receiveMTEInitializationData(@NotNull PacketBuffer buf) {
        int metaTileEntityId = buf.readVarInt();
        setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(metaTileEntityId));
        this.metaTileEntity.onPlacement();
        this.metaTileEntity.receiveInitialSyncData(buf);
        scheduleRenderUpdate();
        this.needToUpdateLightning = true;
    }

    @Override
    public boolean isValid() {
        return !super.isInvalid() && metaTileEntity != null;
    }

    @Override
    public boolean isRemote() {
        return getWorld().isRemote;
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @SuppressWarnings("ConstantConditions") // yes this CAN actually be null
    @Override
    public void markAsDirty() {
        if (getWorld() != null && getPos() != null) {
            getWorld().markChunkDirty(getPos(), this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (metaTileEntity != null) {
            metaTileEntity.onLoad();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (metaTileEntity != null) {
            metaTileEntity.onUnload();
        }
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            onChunkUnloadAE();
        }
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock(); //MetaTileEntityHolder should never refresh (until block changes)
    }

    @Override
    public void rotate(@Nonnull Rotation rotationIn) {
        if (metaTileEntity != null) {
            metaTileEntity.setFrontFacing(rotationIn.rotate(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public void mirror(@Nonnull Mirror mirrorIn) {
        if (metaTileEntity != null) {
            rotate(mirrorIn.toRotation(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (metaTileEntity == null) return false;
        for (EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity && ((IFastRenderMetaTileEntity) cover).shouldRenderInPass(pass)) {
                return true;
            }
        }
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).shouldRenderInPass(pass);
        }
        return false;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).getRenderBoundingBox();
        }
        return new AxisAlignedBB(getPos());
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public boolean hasTESR() {
        if (metaTileEntity == null) return false;
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return true;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity) {
                return true;
            }
        }
        return false;
    }

    public void setCustomName(String customName) {
        if (!getName().equals(customName)) {
            this.customName = customName;
            if (world.isRemote) {
                if (hasCustomName()) {
                    if (nameTagParticle == null) {
                        nameTagParticle = new GTNameTagParticle(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, getName());
                        nameTagParticle.setOnUpdate(p -> {
                            if (isInvalid() || !world.isBlockLoaded(pos, false)) {
                                p.setExpired();
                            }
                        });
                        GTParticleManager.INSTANCE.addEffect(nameTagParticle);
                    } else {
                        nameTagParticle.name = getName();
                    }
                } else {
                    if (nameTagParticle != null) {
                        nameTagParticle.setExpired();
                        nameTagParticle = null;
                    }
                }
            } else {
                markAsDirty();
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return this.customName == null ? "" : this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : metaTileEntity != null ? new TextComponentTranslation(metaTileEntity.getMetaFullName()) : new TextComponentString(this.getName());
    }

    @Nullable
    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public IGridNode getGridNode(@Nonnull AEPartLocation part) {
        // Forbid it connects the faces it shouldn't connect.
        if (this.getCableConnectionType(part) == AECableType.NONE) {
            return null;
        }
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Nonnull
    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        return metaTileEntity == null ? AECableType.NONE : metaTileEntity.getCableConnectionType(part);
    }

    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public void securityBreak() {}

    @Nonnull
    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public IGridNode getActionableNode() {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public AENetworkProxy getProxy() {
        return metaTileEntity == null ? null : metaTileEntity.getProxy();
    }

    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    @Method(modid = GTValues.MODID_APPENG)
    public void gridChanged() {
        if (metaTileEntity != null) {
            metaTileEntity.gridChanged();
        }
    }

    @Method(modid = GTValues.MODID_APPENG)
    public void readFromNBT_AENetwork(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.readFromNBT(data);
        }
    }

    @Method(modid = GTValues.MODID_APPENG)
    public void writeToNBT_AENetwork(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.writeToNBT(data);
        }
    }

    @Method(modid = GTValues.MODID_APPENG)
    void onChunkUnloadAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.onChunkUnload();
        }
    }

    @Method(modid = GTValues.MODID_APPENG)
    void invalidateAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.invalidate();
        }
    }
}
