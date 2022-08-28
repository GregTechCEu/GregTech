package gregtech.api.multitileentity;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.TickableTileEntityBase;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.GTNameTagParticle;
import gregtech.client.particle.GTParticleManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class MultiTileEntity extends TickableTileEntityBase implements IGregTechTileEntity, IAETileEntity, IWorldNameable, IDebugInfoProvider {

    // Start Base MultiTileEntity info ---------

    protected final ResourceLocation tileEntityName;
    protected final short multiTileEntityId;

    // End Base MultiTileEntity info -----------

    // Start Custom Name -----------------------

    private String customName;

    @SideOnly(Side.CLIENT)
    private GTNameTagParticle nameTagParticle;

    // End Custom Name -------------------------

    // Start CPU Time --------------------------

    private static final DecimalFormat tricorderFormat = new DecimalFormat("#.#########");
    private final int[] timeStatistics = new int[20];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;

    // End CPU time ----------------------------

    /**
     * Creates a new MultiTileEntity
     * @param tileEntityName the name of this MultiTileEntity
     * @param multiTileEntityId the id of this MultiTileEntity
     */
    public MultiTileEntity(@Nonnull ResourceLocation tileEntityName, short multiTileEntityId) {
        super();
        this.tileEntityName = tileEntityName;
        this.multiTileEntityId = multiTileEntityId;
    }

    /**
     *
     * @return the unlocalized name of this MultiTileEntity
     */
    @Nonnull
    public final String getUnlocalizedName() {
        return this.tileEntityName.toString();
    }

    /**
     *
     * @return true if this is a valid TileEntity, otherwise false
     */
    public final boolean isValid() {
        return !this.isInvalid();
    }

    @Override
    public void update() {
        updateTimeStatistics();

        //increment only after current tick, so tile entities will get first tick as timer == 0
        //and update their settings which depend on getTimer() % N properly
        super.update();
    }

    private void updateTimeStatistics() {
        long tickTime = System.nanoTime();
        if (!getWorld().isRemote && this.isValid()) {
            tickTime = System.nanoTime() - tickTime;
            if (timeStatistics.length > 0) {
                timeStatistics[timeStatisticsIndex] = (int) tickTime;
                timeStatisticsIndex = (timeStatisticsIndex + 1) % timeStatistics.length;
            }
            if (tickTime > 100_000_000L && this.doTickProfileMessage() && lagWarningCount++ < 10) {
                GTLog.logger.warn(String.format("WARNING: Possible Lag Source at [%s, %s, %s] in dimension %s with %s ns caused by an instance of %s", getPos().getX(), getPos().getY(), getPos().getZ(), getWorld().provider.getDimension(), tickTime, this.getClass()));
            }
        }
    }

    /**
     *
     * @return true if this should log lag warnings, otherwise false
     */
    public boolean doTickProfileMessage() {
        return true;
    }

    /**
     * Notifies the neighboring blocks that this MultiTileEntity has updated
     */
    @Override
    public final void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), false);
    }

    private void setCustomName(String customName) {
        if (!getCustomName().equals(customName)) {
            this.customName = customName;
            if (world.isRemote) {
                if (hasCustomName()) {
                    if (nameTagParticle == null) {
                        nameTagParticle = new GTNameTagParticle(world, getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, getCustomName());
                        nameTagParticle.setOnUpdate(p -> {
                            if (isInvalid() || !GTUtility.isPosChunkLoaded(getWorld(), getPos())) p.setExpired();
                        });
                        GTParticleManager.INSTANCE.addEffect(nameTagParticle);
                    } else {
                        nameTagParticle.name = getCustomName();
                    }
                } else {
                    if (nameTagParticle != null) {
                        nameTagParticle.setExpired();
                        nameTagParticle = null;
                    }
                }
            } else {
                this.markDirty();
            }
        }
    }

    /**
     * This method is marked as Deprecated to avoid potential confusion.
     * Use {@link MultiTileEntity#getCustomName()} instead
     *
     * @return the custom name of this MultiTileEntity
     */
    @Deprecated
    @Nonnull
    @Override
    public String getName() {
        return this.customName == null ? "" : this.customName;
    }

    @Nonnull
    public final String getCustomName() {
        return this.getName();
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            invalidateAE();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            onAEChunkUnload();
        }
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        if (this.hasCustomName()) {
            return new TextComponentString(this.getCustomName());
        }

        return new TextComponentTranslation(getUnlocalizedName());
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return getWorld().isRemote;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.customName = compound.getString("CustomName");
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            readAENetworkFromNBT(compound);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("CustomName", this.getCustomName());
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            writeAENetworkToNBT(compound);
        }
        return compound;
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {/**/}

    @Override
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.setCustomName(buf.readString(Short.MAX_VALUE));
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {/**/}

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || this.getCapability(capability, facing) != null;
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDebugInfo(@Nullable EntityPlayer player, int logLevel) {
        List<ITextComponent> list = new ArrayList<>();
        if (logLevel > 2) {
            if (this.isValid()) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getUnlocalizedName()).setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_valid").setStyle(new Style().setColor(TextFormatting.GREEN))
                ));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_machine",
                        new TextComponentTranslation(getUnlocalizedName()).setStyle(new Style().setColor(TextFormatting.BLUE)),
                        new TextComponentTranslation("behavior.tricorder.debug_machine_invalid").setStyle(new Style().setColor(TextFormatting.RED))
                ));
            }
        }

        if (logLevel > 1) {
            if (timeStatistics.length > 0) {
                double averageTickTime = 0;
                double worstTickTime = 0;
                for (int tickTime : timeStatistics) {
                    averageTickTime += tickTime;
                    if (tickTime > worstTickTime) {
                        worstTickTime = tickTime;
                    }
                    // Uncomment this line to print out tick-by-tick times.
                    // list.add(new TextComponentTranslation("tickTime " + tickTime));
                }
                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load",
                        new TextComponentTranslation(GTUtility.formatNumbers(averageTickTime / timeStatistics.length)).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                        new TextComponentTranslation(GTUtility.formatNumbers(timeStatistics.length)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(GTUtility.formatNumbers(worstTickTime)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
                list.add(new TextComponentTranslation("behavior.tricorder.debug_cpu_load_seconds", tricorderFormat.format(worstTickTime / 1000000000)));
            }
            if (lagWarningCount > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.debug_lag_count",
                        new TextComponentTranslation(GTUtility.formatNumbers(lagWarningCount)).setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(GTUtility.formatNumbers(100_000_000L)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
            }
        }
        return list;
    }
}
