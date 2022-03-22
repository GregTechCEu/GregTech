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
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.Block;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityWorldAccelerator extends TieredMetaTileEntity implements IControllable {

    private static Class<?> cofhTileClass;

    private static boolean considerTile(TileEntity tile) {
        // TODO interface for this?
        if (tile instanceof IGregTechTileEntity || tile instanceof TileEntityPipeBase) {
            return false;
        }
        if (cofhTileClass == null) {
            try {
                cofhTileClass = Class.forName("cofh.thermalexpansion.block.device.TileDeviceBase");
            } catch (Exception ignored) {}
        }
        return cofhTileClass == null || !cofhTileClass.isInstance(tile);
    }

    private final long energyPerTick;
    private final int speed;

    private boolean tileMode = false;
    private boolean isActive = false;
    private boolean isPaused = false;
    private int lastTick;
    private Supplier<Iterable<BlockPos.MutableBlockPos>> range;

    public MetaTileEntityWorldAccelerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        //consume 8 amps
        this.energyPerTick = GTValues.V[tier] * getMaxInputOutputAmperage();
        this.lastTick = 0;
        this.speed = (int) Math.pow(2, tier);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWorldAccelerator(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in", getMaxInputOutputAmperage()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.description"));
        int area = getTier() * 2;
        tooltip.add(I18n.format("gregtech.machine.world_accelerator.area", area, area));
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 8L;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (isPaused) {
                if (isActive) {
                    setActive(false);
                }
                return;
            }
            if (energyContainer.getEnergyStored() < energyPerTick) {
                if (isActive) {
                    setActive(false);
                }
                return;
            }
            if (!isActive) {
                setActive(true);
            }
            int currentTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            if (currentTick != lastTick) { // Prevent other tick accelerators from accelerating us
                World world = getWorld();
                BlockPos currentPos = getPos();
                lastTick = currentTick;
                if (isTEMode()) {
                    energyContainer.removeEnergy(energyPerTick);
                    for (EnumFacing neighbourFace : EnumFacing.VALUES) {
                        TileEntity neighbourTile = world.getTileEntity(currentPos.offset(neighbourFace));
                        if (neighbourTile instanceof ITickable && !neighbourTile.isInvalid() && considerTile(neighbourTile)) {
                            ITickable neighbourTickTile = (ITickable) neighbourTile;
                            for (int i = 0; i < speed; i++) {
                                neighbourTickTile.update();
                            }
                        }
                    }
                } else {
                    energyContainer.removeEnergy(energyPerTick / 2);
                    if (range == null) {
                        int area = getTier() * 2;
                        range = () -> BlockPos.getAllInBoxMutable(currentPos.add(-area, -area, -area), currentPos.add(area, area, area));
                    }
                    for (BlockPos.MutableBlockPos pos : range.get()) {
                        if (pos.getY() > 256 || pos.getY() < 0) { // Early termination
                            continue;
                        }
                        if (world.isBlockLoaded(pos)) {
                            for (int i = 0; i < speed; i++) {
                                if (GTValues.RNG.nextInt(100) < getTier()) {
                                    // Rongmario:
                                    // randomTick instead of updateTick since some modders can mistake where to put their code.
                                    // Fresh IBlockState before every randomTick, this could easily change after every randomTick call
                                    IBlockState state = world.getBlockState(pos);
                                    Block block = state.getBlock();
                                    if (block.getTickRandomly()) {
                                        block.randomTick(world, pos.toImmutable(), state, world.rand);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (isTEMode()) {
            Textures.WORLD_ACCELERATOR_TE_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, isWorkingEnabled());
        } else {
            Textures.WORLD_ACCELERATOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, isWorkingEnabled());
        }
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
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(tileMode));
            notifyBlockUpdate();
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
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
        if (dataId == SYNC_TILE_MODE) {
            this.tileMode = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return !isPaused;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        isPaused = !b;
        notifyBlockUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
}
