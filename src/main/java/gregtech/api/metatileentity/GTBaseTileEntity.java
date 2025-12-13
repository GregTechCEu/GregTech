package gregtech.api.metatileentity;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.cover.Cover;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.Mods;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.GTNameTagParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.common.ConfigHolder;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "appeng.api.networking.security.IActionHost",
                            modid = Mods.Names.APPLIED_ENERGISTICS2,
                            striprefs = true),
        @Optional.Interface(iface = "appeng.me.helpers.IGridProxyable",
                            modid = Mods.Names.APPLIED_ENERGISTICS2,
                            striprefs = true) })
public abstract class GTBaseTileEntity extends TickableTileEntityBase implements
                                       IGregTechTileEntity,
                                       IWorldNameable, IActionHost, IGridProxyable {

    private boolean needToUpdateLightning = false;
    private String customName;
    @SideOnly(Side.CLIENT)
    private GTNameTagParticle nameTagParticle;

    public static final int TRACKED_TICKS = 20;
    private final int[] timeStatistics = new int[TRACKED_TICKS];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;
    protected static final DecimalFormat tricorderFormat = new DecimalFormat("#.#########");

    private static final ThreadLocal<Map<BlockPos, IGregTechTileEntity>> tileMap = ThreadLocal.withInitial(
            Object2ObjectArrayMap::new);
    private static final ThreadLocal<IGregTechTileEntity> placingTE = new ThreadLocal<>();

    public static void setPlacingTE(IGregTechTileEntity mte) {
        if (mte == null) {
            placingTE.remove();
        } else {
            placingTE.set(mte);
        }
    }

    public static IGregTechTileEntity getPlacingTE() {
        return placingTE.get();
    }

    public static MetaTileEntity copyPlacingMTE() {
        if (placingTE.get() == null) return null;
        return getPlacingTE().getMetaTileEntity().createMetaTileEntity(null);
    }

    public static void storeTE(IGregTechTileEntity mte) {
        storeTE(mte.pos(), mte);
    }

    public static void storeTE(BlockPos pos, IGregTechTileEntity mte) {
        tileMap.get().put(pos, mte);
        GTLog.logger.warn("stored mte {} at pos {}", mte.getMetaTileEntity().getMetaID(), pos);
    }

    public static void removeTE(IGregTechTileEntity mte) {
        tileMap.get().remove(mte.pos());
    }

    public static MetaTileEntity getTEByPos(BlockPos pos) {
        return tileMap.get().get(pos).getMetaTileEntity();
    }

    public static boolean hasTE(BlockPos pos) {
        return tileMap.get().containsKey(pos);
    }

    public static void clearTileMap() {
        tileMap.get().clear();
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public final void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        storeTE(this);
        customName = compound.getString(GregtechDataCodes.CUSTOM_NAME);
        if (compound.hasKey("MetaId", Constants.NBT.TAG_STRING)) {
            readMTETag(compound.getCompoundTag("MetaTileEntity"));
            if (Mods.AppliedEnergistics2.isModLoaded()) {
                readFromNBT_AENetwork(compound);
            }
        }
    }

    @NotNull
    @Override
    public final NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(GregtechDataCodes.CUSTOM_NAME, getName());
        compound.setString("MetaId", getMetaID().toString());
        NBTTagCompound metaTileEntityData = new NBTTagCompound();
        writeMTETag(metaTileEntityData);
        compound.setTag("MetaTileEntity", metaTileEntityData);
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            writeToNBT_AENetwork(compound);
        }
        return compound;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            invalidateAE();
        }
        removeTE(this);
    }

    @Override
    public void update() {
        long tickTime = System.nanoTime();

        updateMTE();

        if (this.needToUpdateLightning) {
            getWorld().checkLight(getPos());
            this.needToUpdateLightning = false;
        }

        if (!world.isRemote && isValid()) {
            tickTime = System.nanoTime() - tickTime;
            if (timeStatistics.length > 0) {
                timeStatistics[timeStatisticsIndex] = (int) tickTime;
                timeStatisticsIndex = (timeStatisticsIndex + 1) % timeStatistics.length;
            }
            if (tickTime > 100_000_000L && doTickProfileMessage() && lagWarningCount++ < 10)
                GTLog.logger.warn("WARNING: Possible Lag Source at [" + getPos().getX() + ", " + getPos().getY() +
                        ", " + getPos().getZ() + "] in Dimension " + world.provider.getDimension() + " with " +
                        tickTime + "ns caused by an instance of " + getClass());
        }

        // increment only after current tick, so meta tile entities will get first tick as timer == 0
        // and update their settings which depend on getTimer() % N properly
        super.update();
    }

    public ArrayList<ITextComponent> getDebugInfo(EntityPlayer player, int logLevel) {
        ArrayList<ITextComponent> list = new ArrayList<>();
        if (logLevel > 2) {
            if (isValid()) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getMetaID().toString())
                                .setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_valid")
                                .setStyle(new Style().setColor(TextFormatting.GREEN))));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getMetaID().toString())
                                .setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_invalid")
                                .setStyle(new Style().setColor(TextFormatting.RED))));
            }
        }
        if (logLevel > 1) {
            double[] timeStats = getTimeStatistics();
            if (timeStats != null) {
                double averageTickTime = timeStats[0];
                double worstTickTime = timeStats[1];

                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(averageTickTime / timeStatistics.length))
                                        .setStyle(new Style().setColor(TextFormatting.YELLOW)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(timeStatistics.length))
                                .setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(worstTickTime))
                                .setStyle(new Style().setColor(TextFormatting.RED))));
                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load_seconds",
                        tricorderFormat.format(worstTickTime / 1000000000)));
            }

            if (lagWarningCount > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_lag_count",
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(lagWarningCount))
                                .setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(TextFormattingUtil.formatNumbers(100_000_000L))
                                .setStyle(new Style().setColor(TextFormatting.RED))));
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
        // below this might not be necessary anymore ?
        buf.writeBoolean(true);
        buf.writeVarInt(getRegistry().getNetworkId());
        buf.writeVarInt(getRegistry().getIdByObjectName(getMetaID()));
        writeInitialSyncDataMTE(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        setCustomName(buf.readString(Short.MAX_VALUE));
        buf.readBoolean();
        receiveMTEInitializationData(buf);
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buffer) {
        // if (discriminator == INITIALIZE_MTE) {
        // receiveMTEInitializationData(buffer);
        // }
    }

    /**
     * Sets and initializes the MTE
     *
     * @param buf the buffer to read data from
     */
    private void receiveMTEInitializationData(@NotNull PacketBuffer buf) {
        int networkId = buf.readVarInt();
        int metaTileEntityId = buf.readVarInt();
        this.onPlacement();
        this.receiveInitialSyncDataMTE(buf);
        scheduleRenderUpdate();
        this.needToUpdateLightning = true;
    }

    @Override
    public boolean isValid() {
        return !super.isInvalid();
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
    }

    @Override
    public final void onChunkUnload() {
        super.onChunkUnload();
        removeTE(this);
        onUnload();
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            onChunkUnloadAE();
        }
    }

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, IBlockState oldState,
                                 IBlockState newState) {
        // MetaTileEntityHolder should never refresh (until block changes)
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void rotate(@NotNull Rotation rotationIn) {
        setFrontFacing(rotationIn.rotate(getFrontFacing()));
    }

    @Override
    public void mirror(@NotNull Mirror mirrorIn) {
        rotate(mirrorIn.toRotation(getFrontFacing()));
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity fastRender && fastRender.shouldRenderInPass(pass)) {
                return true;
            }
        }
        if (this instanceof IFastRenderMetaTileEntity fastRender) {
            return fastRender.shouldRenderInPass(pass);
        }
        return false;
    }

    @NotNull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this instanceof IFastRenderMetaTileEntity fastRender) {
            return fastRender.getRenderBoundingBox();
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
        if (this instanceof IFastRenderMetaTileEntity) {
            return true;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            Cover cover = getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getUIColorOverride() {
        if (ConfigHolder.client.useSprayCanColorInUI) {
            return getPaintingColor();
        }
        return -1;
    }

    public void setCustomName(String customName) {
        if (!getName().equals(customName)) {
            this.customName = customName;
            if (world.isRemote) {
                updateNameTagParticle();
            } else {
                markAsDirty();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateNameTagParticle() {
        if (hasCustomName()) {
            if (nameTagParticle == null) {
                nameTagParticle = new GTNameTagParticle(this, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
                GTParticleManager.INSTANCE.addEffect(nameTagParticle);
            }
        } else {
            if (nameTagParticle != null) {
                nameTagParticle.setExpired();
                nameTagParticle = null;
            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return this.customName == null ? "" : this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ?
                new TextComponentString(this.getName()) :
                new TextComponentTranslation(getMetaFullName());
    }

    @Override
    public MetaTileEntity setMetaTileEntity(@NotNull MetaTileEntity metaTileEntity,
                                            @Nullable NBTTagCompound tagCompound) {
        return getMetaTileEntity();
    }

    // this should return itself

    @Override
    public abstract MetaTileEntity getMetaTileEntity();

    // MetaTileEntity Methods

    protected abstract void writeInitialSyncDataMTE(@NotNull PacketBuffer buf);

    public abstract void receiveInitialSyncDataMTE(@NotNull PacketBuffer buf);

    protected abstract void onPlacement();

    protected abstract void updateMTE();

    public abstract NBTTagCompound writeMTETag(NBTTagCompound tagCompound);

    public abstract void readMTETag(NBTTagCompound tagCompound);

    protected abstract MTERegistry getRegistry();

    protected abstract boolean doTickProfileMessage();

    protected abstract void onUnload();

    protected abstract void setFrontFacing(EnumFacing facing);

    protected abstract EnumFacing getFrontFacing();

    protected abstract Cover getCoverAtSide(EnumFacing side);

    public abstract int getPaintingColor();

    public abstract ResourceLocation getMetaID();

    public String getMetaName() {
        return String.format("%s.machine.%s", getMetaID().getNamespace(), getMetaID().getPath());
    }

    public final String getMetaFullName() {
        return getMetaName() + ".name";
    }

    // AE2 Methods

    @Nullable
    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public IGridNode getGridNode(@NotNull AEPartLocation part) {
        // Forbid it connects the faces it shouldn't connect.
        if (this.getCableConnectionType(part) == AECableType.NONE) {
            return null;
        }
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @NotNull
    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public abstract AECableType getCableConnectionType(@NotNull AEPartLocation part);

    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void securityBreak() {}

    @NotNull
    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public IGridNode getActionableNode() {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public abstract AENetworkProxy getProxy();

    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void gridChanged() {
        // if (metaTileEntity != null) {
        // metaTileEntity.gridChanged();
        // }
    }

    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void readFromNBT_AENetwork(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.readFromNBT(data);
        }
    }

    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    public void writeToNBT_AENetwork(NBTTagCompound data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.writeToNBT(data);
        }
    }

    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    void onChunkUnloadAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.onChunkUnload();
        }
    }

    @Optional.Method(modid = Mods.Names.APPLIED_ENERGISTICS2)
    void invalidateAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.invalidate();
        }
    }
}
