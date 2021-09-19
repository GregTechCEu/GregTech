package gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.render.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

public class MetaTileEntityWorldAccelerator extends TieredMetaTileEntity implements IControllable {

    private static Class<?> clazz;

    static {
        try {
            clazz = Class.forName("cofh.core.block.TileCore");
        } catch (Exception ignored) {}
    }

    private final long energyPerTick;
    private boolean tileMode = false;
    private boolean isActive = false;
    private boolean isPaused = false;
    private int lastTick;

    public MetaTileEntityWorldAccelerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        //consume 8 amps
        this.energyPerTick = GTValues.V[tier] * getMaxInputOutputAmperage();
        this.lastTick = 0;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityWorldAccelerator(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in", getMaxInputOutputAmperage()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.description"));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.area", getArea(), getArea()));
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 8L;
    }

    // TODO This could use a re-write
    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && lastTick != FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter()) {
            lastTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            if (isPaused) {
                if (isActive)
                    setActive(false);
                return;
            }
            if (energyContainer.getEnergyStored() < energyPerTick) {
                if (isActive)
                    setActive(false);
                return;
            }
            if (!isActive)
                setActive(true);
            energyContainer.removeEnergy(energyPerTick);
            WorldServer world = (WorldServer) this.getWorld();
            BlockPos worldAcceleratorPos = getPos();
            if (isTEMode()) {
                BlockPos[] neighbours = new BlockPos[]{worldAcceleratorPos.down(), worldAcceleratorPos.up(), worldAcceleratorPos.north(), worldAcceleratorPos.south(), worldAcceleratorPos.east(), worldAcceleratorPos.west()};
                for (BlockPos neighbour : neighbours) {
                    TileEntity targetTE = world.getTileEntity(neighbour);
                    if (targetTE == null || targetTE instanceof TileEntityPipeBase || targetTE instanceof MetaTileEntityHolder) {
                        continue;
                    }
                    boolean horror = false;
                    if (clazz != null && targetTE instanceof ITickable) {
                        horror = clazz.isInstance(targetTE);
                    }
                    if (targetTE instanceof ITickable && (!horror || !world.isRemote)) {
                        IntStream.range(0, (int) Math.pow(2, getTier())).forEach(value -> ((ITickable) targetTE).update());
                    }
                }
            } else {
                BlockPos upperConner = worldAcceleratorPos.north(getTier()).east(getTier());
                for (int x = 0; x < getArea(); x++) {
                    BlockPos row = upperConner.south(x);
                    for (int y = 0; y < getArea(); y++) {
                        BlockPos cell = row.west(y);

                        IBlockState targetBlock = world.getBlockState(cell);
                        IntStream.range(0, (int) Math.pow(2, getTier())).forEach(value -> {
                            if (GTValues.RNG.nextInt(100) == 0) {
                                if (targetBlock.getBlock().getTickRandomly()) {
                                    targetBlock.getBlock().randomTick(world, cell, targetBlock, world.rand);
                                }
                            }
                        });
                    }
                }
            }
        }

    }

    public int getArea() {
        return (getTier() * 2) + 1;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (isTEMode())
            Textures.WORLD_ACCELERATOR_TE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
        else
            Textures.WORLD_ACCELERATOR_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote) {
            if (isTEMode()) {
                setTEMode(false);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.world_accelerator.mode_entity"), false);
            } else {
                setTEMode(true);
                playerIn.sendStatusMessage(new TextComponentTranslation("gregtech.machine.world_accelerator.mode_tile"), false);
            }
        }
        return true;
    }

    public void setTEMode(boolean inverted) {
        tileMode = inverted;
        if (!getWorld().isRemote) {
            reinitializeEnergyContainer();
            writeCustomData(100, b -> b.writeBoolean(tileMode));
            getHolder().notifyBlockUpdate();
            markDirty();
        }
    }

    public boolean isTEMode() {
        return tileMode;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("TileMode", tileMode);
        data.setBoolean("isPaused", isPaused);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        tileMode = data.getBoolean("TileMode");
        isPaused = data.getBoolean("isPaused");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(tileMode);
        buf.writeBoolean(isPaused);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.tileMode = buf.readBoolean();
        this.isPaused = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
        if (dataId == 100) {
            this.tileMode = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return !isPaused;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        isPaused = !b;
        getHolder().notifyBlockUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
}
