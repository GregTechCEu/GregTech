package gregtech.common.pipelike.cable.tile;

import codechicken.lib.vec.Cuboid6;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTUtility;
import gregtech.api.util.PerTickLongCounter;
import gregtech.api.util.TaskScheduler;
import gregtech.client.particle.GTOverheatParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.EnergyNetHandler;
import gregtech.common.pipelike.cable.net.WorldENet;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties> implements IDataInfoProvider {

    private final EnumMap<EnumFacing, EnergyNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter(0);
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter(0, 20);
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter(0, 20);
    private EnergyNetHandler defaultHandler;
    // the EnergyNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IEnergyContainer clientCapability = IEnergyContainer.DEFAULT;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);
    @SideOnly(Side.CLIENT)
    private GTOverheatParticle particle;
    private int heatQueue;
    private int temperature = 293;
    private final int meltTemp = 3000;
    private boolean isTicking = false;

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
        EnergyNet net = getEnergyNet();
        if (net == null) {
            return;
        }
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
            if (temperature > 293) {
                TaskScheduler.scheduleTask(world, this::update);
            }
        }
    }

    /**
     * Should only be called internally
     *
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(long amps, long voltage) {
        if (voltage > maxVoltageCounter.get(world)) {
            maxVoltageCounter.set(world, voltage);
        }
        averageVoltageCounter.increment(world, voltage);
        averageAmperageCounter.increment(world, amps);

        int dif = (int) (averageAmperageCounter.getLast(world) - getMaxAmperage());
        if (dif > 0) {
            applyHeat(dif * 40);
            return true;
        }

        return false;
    }

    public void applyHeat(int amount) {
        heatQueue += amount;
        if (!world.isRemote && !isTicking && temperature + heatQueue > 293) {
            TaskScheduler.scheduleTask(world, this::update);
            isTicking = true;
        }
    }

    private boolean update() {
        if (heatQueue > 0) {
            // if received heat from overvolting or overamping, add heat
            setTemperature(temperature + heatQueue);
        }

        if (temperature >= meltTemp) {
            // cable melted
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            isTicking = false;
            return false;
        }

        if (temperature <= 293) {
            isTicking = false;
            return false;
        }

        if (getPipeType().insulationLevel >= 0 && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1) {
            // insulation melted
            uninsulate();
            isTicking = false;
            return false;
        }

        if (heatQueue == 0) {
            // otherwise cool down
            setTemperature((int) (temperature - Math.pow(temperature - 293, 0.35)));
        } else {
            heatQueue = 0;
        }
        return true;
    }

    private void uninsulate() {
        int temp = temperature;
        setTemperature(293);
        int index = getPipeType().insulationLevel;
        BlockCable newBlock = MetaBlocks.CABLES[index];
        world.setBlockState(pos, newBlock.getDefaultState());
        TileEntityCable newCable = (TileEntityCable) world.getTileEntity(pos);
        if (newCable != null) { // should never be null
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
            writeCustomData(100, buf -> buf.writeVarInt(temperature));
        } else {
            if (temperature <= 293) {
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

    public int getDefaultTemp() {
        return 293;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMeltTemp() {
        return meltTemp;
    }

    @SideOnly(Side.CLIENT)
    public void createParticle() {
        particle = new GTOverheatParticle(world, pos, meltTemp, getPipeBoxes(), getPipeType().insulationLevel >= 0);
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void killParticle() {
        if (isParticleAlive()) {
            particle.setExpired();
            particle = null;
        }
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

    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (world.isRemote)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(clientCapability);
            if (handlers.size() == 0)
                initHandlers();
            checkNetwork();
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }


    public void checkNetwork() {
        if (defaultHandler != null) {
            EnergyNet current = getEnergyNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (EnergyNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    private EnergyNet getEnergyNet() {
        if (world == null || world.isRemote)
            return null;
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(getPos()))
            return currentEnergyNet; //return current net if it is still valid
        WorldENet worldENet = WorldENet.getWorldENet(getWorld());
        currentEnergyNet = worldENet.getNetFromPos(getPos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0x404040;
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (discriminator == 100) {
            setTemperature(buf.readVarInt());
        } else {
            super.receiveCustomData(discriminator, buf);
            if (isParticleAlive() && discriminator == GregtechDataCodes.UPDATE_CONNECTIONS) {
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

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Temp", temperature);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        temperature = compound.getInteger("Temp");
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("behavior.tricorder.eut_per_sec",
                new TextComponentTranslation(GTUtility.formatNumbers(this.getAverageVoltage())).setStyle(new Style().setColor(TextFormatting.RED))
        ));
        list.add(new TextComponentTranslation("behavior.tricorder.amp_per_sec",
                new TextComponentTranslation(GTUtility.formatNumbers(this.getAverageAmperage())).setStyle(new Style().setColor(TextFormatting.RED))
        ));
        return list;
    }
}
