package gregtech.common.pipelike.cable.tile;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NodeLossResult;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.edge.NetFlowEdge;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.TaskScheduler;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.GTOverheatParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNetHandler;
import gregtech.common.pipelike.cable.net.WorldEnergyNet;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.vec.Cuboid6;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties, NetFlowEdge>
                             implements IDataInfoProvider {

    private final EnumMap<EnumFacing, EnergyNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter();
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter();
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter();
    private EnergyNetHandler defaultHandler;
    // the EnergyNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IEnergyContainer clientCapability = IEnergyContainer.DEFAULT;
    @SideOnly(Side.CLIENT)
    private GTOverheatParticle particle;
    private int temperature = getDefaultTemp();
    private boolean isTicking = false;

    public long getWorldTime() {
        return hasWorld() ? getWorld().getTotalWorldTime() : 0L;
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        WorldEnergyNet net = WorldEnergyNet.getWorldEnergyNet(getPipeWorld());
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new EnergyNetHandler(net, this, facing));
        }
        defaultHandler = new EnergyNetHandler(net, this, null);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote) {
            setTemperature(temperature);
            if (temperature > getDefaultTemp()) {
                TaskScheduler.scheduleTask(world, this::update);
            }
        }
    }

    public NodeLossResult applyHeat(int amount, boolean simulate) {
        double loss = 1;
        Consumer<NetNode<?, ?, ?>> destructionResult = null;

        if (temperature + amount >= getMeltTemp()) {
            // cable melted
            destructionResult = node -> {
                isTicking = false;
                getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
                if (!getWorld().isRemote) {
                    ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5 + getWorld().rand.nextInt(3), 0.0, 0.0, 0.0, 0.1);
                }
            };
            loss = 0;
        }
        if (!simulate) setTemperature(temperature + amount);
        return new NodeLossResult(destructionResult == null ? node -> {} : destructionResult, loss);
    }

    private boolean update() {
        // thicker cables cool faster
        setTemperature((int) (temperature - Math.pow(temperature - getDefaultTemp(), 0.35) *
                Math.sqrt(this.getPipeType().getThickness()) * 4));

        if (getPipeType().insulationLevel >= 0 && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1) {
            // insulation melted
            uninsulate();
            isTicking = false;
        }
        return isTicking;
    }

    private void uninsulate() {
        int temp = temperature;
        setTemperature(getDefaultTemp());
        int index = getPipeType().insulationLevel;
        BlockCable newBlock = MetaBlocks.CABLES.get(getPipeBlock().getMaterialRegistry().getModid())[index];
        world.setBlockState(pos, newBlock.getDefaultState());
        TileEntityCable newCable = (TileEntityCable) world.getTileEntity(pos);
        if (newCable != null) { // should never be null
            newCable.transferDataFrom(this);
            newCable.setPipeData(newBlock, newBlock.getItemPipeType(null), getPipeMaterial());
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (isConnected(facing)) {
                    newCable.setConnection(facing, true, true);
                }
            }
            newCable.setTemperature(temp);
            if (!newCable.isTicking) {
                TaskScheduler.scheduleTask(world, newCable::update);
                newCable.isTicking = true;
            }
        }
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        world.checkLight(pos);
        if (!world.isRemote) {
            writeCustomData(CABLE_TEMPERATURE, buf -> buf.writeVarInt(temperature));
            if (!isTicking && temperature > getDefaultTemp()) {
                isTicking = true;
                TaskScheduler.scheduleTask(getWorld(), this::update);
            } else if (isTicking && temperature <= getDefaultTemp()) {
                isTicking = false;
            }
        } else {
            // TODO fix particle sometimes not rendering after world load
            if (temperature <= getDefaultTemp()) {
                if (isParticleAlive())
                    particle.setExpired();
            } else {
                if (!isParticleAlive()) {
                    createParticle();
                }
                particle.setTemperature(temperature);
            }
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<Insulation, WireProperties, NetFlowEdge> tileEntity) {
        super.transferDataFrom(tileEntity);
    }

    public int getDefaultTemp() {
        return 293;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMeltTemp() {
        return this.getNodeData().getMeltTemperature();
    }

    @SideOnly(Side.CLIENT)
    public void createParticle() {
        particle = new GTOverheatParticle(this, getMeltTemp(), getPipeBoxes(), getPipeType().insulationLevel >= 0);
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void killParticle() {
        if (isParticleAlive()) {
            particle.setExpired();
            particle = null;
        }
    }

    public void contributeAmperageFlow(long amperage) {
        averageAmperageCounter.increment(getWorld(), amperage);
    }

    public void contributeVoltageFlow(long voltage) {
        if (voltage > maxVoltageCounter.get(getWorld())) maxVoltageCounter.set(getWorld(), voltage);
        averageVoltageCounter.set(getWorld(), maxVoltageCounter.get(getWorld()));
    }

    public double getAverageAmperage() {
        return averageAmperageCounter.getAverage(getWorld());
    }

    public long getCurrentMaxVoltage() {
        return maxVoltageCounter.get(getWorld());
    }

    public double getAverageVoltage() {
        return averageVoltageCounter.getAverage(getWorld());
    }

    public long getMaxAmperage() {
        return getNodeData().getAmperage();
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Deprecated
    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (world.isRemote)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(clientCapability);
            if (handlers.isEmpty())
                initHandlers();
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0x404040;
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (discriminator == CABLE_TEMPERATURE) {
            setTemperature(buf.readVarInt());
        } else {
            super.receiveCustomData(discriminator, buf);
            if (isParticleAlive() && discriminator == UPDATE_CONNECTIONS) {
                particle.updatePipeBoxes(getPipeBoxes());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isParticleAlive() {
        return particle != null && particle.isAlive();
    }

    protected List<Cuboid6> getPipeBoxes() {
        List<Cuboid6> pipeBoxes = new ArrayList<>();
        float thickness = getPipeType().getThickness();
        if ((getConnections() & 63) < 63) {
            pipeBoxes.add(BlockPipe.getSideBox(null, thickness));
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (isConnected(facing))
                pipeBoxes.add(BlockPipe.getSideBox(facing, thickness));
        }
        return pipeBoxes;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Temp", temperature);
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        temperature = compound.getInteger("Temp");
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("behavior.tricorder.eut_per_sec",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getAverageVoltage()))
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        list.add(new TextComponentTranslation("behavior.tricorder.amp_per_sec",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.getAverageAmperage()))
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        return list;
    }
}
