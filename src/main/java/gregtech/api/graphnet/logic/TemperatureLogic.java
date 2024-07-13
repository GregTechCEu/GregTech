package gregtech.api.graphnet.logic;

import gregtech.api.graphnet.pipenet.NodeLossResult;

import gregtech.api.graphnet.pipenet.physical.IBurnable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.network.PacketBuffer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TemperatureLogic implements INetLogicEntry<TemperatureLogic, NBTTagCompound> {

    public static final TemperatureLogic INSTANCE = new TemperatureLogic();

    public static final int DEFAULT_TEMPERATURE = 293;

    @Nullable
    private NetLogicData owner;

    private int temperatureMaximum;
    private @Nullable Integer partialBurnTemperature;
    private int temperatureMinimum;
    private float energy;
    private int thermalMass;

    private TemperatureRestorationFunction temperatureRestorationFunction;
    private float restorationSpeedFactor;
    private long lastRestorationTick;

    private TemperatureLogic() {}

    public TemperatureLogic getWith(TemperatureRestorationFunction temperatureRestorationFunction, float restorationSpeedFactor, int temperatureMaximum) {
        return getWith(temperatureRestorationFunction, restorationSpeedFactor, temperatureMaximum, null);
    }

    public TemperatureLogic getWith(TemperatureRestorationFunction temperatureRestorationFunction, float restorationSpeedFactor, int temperatureMaximum, @Nullable Integer partialBurnTemperature) {
        return getWith(temperatureRestorationFunction, restorationSpeedFactor, temperatureMaximum, partialBurnTemperature, 1);
    }

    public TemperatureLogic getWith(TemperatureRestorationFunction temperatureRestorationFunction, float restorationSpeedFactor, int temperatureMaximum, @Nullable Integer partialBurnTemperature, int temperatureMinimum) {
        return getWith(temperatureRestorationFunction, restorationSpeedFactor, temperatureMaximum, partialBurnTemperature, temperatureMinimum, 1000);
    }

    public TemperatureLogic getWith(TemperatureRestorationFunction temperatureRestorationFunction, float restorationSpeedFactor, int temperatureMaximum, @Nullable Integer partialBurnTemperature, int temperatureMinimum, int thermalMass) {
        return getNew()
                .setTemperatureRestorationFunction(temperatureRestorationFunction)
                .setRestorationSpeedFactor(restorationSpeedFactor)
                .setTemperatureMaximum(temperatureMaximum)
                .setPartialBurnTemperature(partialBurnTemperature)
                .setTemperatureMinimum(temperatureMinimum)
                .setThermalMass(thermalMass);
    }

    public TemperatureLogic getNew() {
        return new TemperatureLogic();
    }

    public boolean aboveMax(long tick) {
        return this.getTemperature(tick) > this.temperatureMaximum;
    }

    public boolean belowMin(long tick) {
        return this.getTemperature(tick) < this.temperatureMinimum;
    }

    @Nullable
    public NodeLossResult getLossResult(long tick) {
        if (aboveMax(tick)) {
            return new NodeLossResult(n -> {
                World world = n.getNet().getWorld();
                BlockPos pos = n.getEquivalencyData();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IBurnable burnable) {
                    burnable.fullyBurn(state, world, pos);
                } else {
                    world.setBlockToAir(pos);
                }
            }, l -> 0L);
        } else if (partialBurnTemperature != null && getTemperature(tick) > partialBurnTemperature) {
            return new NodeLossResult(n -> {
                World world = n.getNet().getWorld();
                BlockPos pos = n.getEquivalencyData();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IBurnable burnable) {
                    burnable.partialBurn(state, world, pos);
                }
            }, l -> (long) (l * 0.5));
        } else {
            return null;
        }
    }

    public void applyThermalEnergy(float energy, long tick) {
        restoreTemperature(tick);
        // since the decay logic is synced and deterministic,
        // the only time client and server will desync is on external changes.
        if (this.owner != null) this.owner.markLogicEntryAsUpdated(this, false);
        this.energy += energy;
    }

    public int getTemperature(long tick) {
        restoreTemperature(tick);
        return (int) (this.energy / this.thermalMass) + DEFAULT_TEMPERATURE;
    }

    private void restoreTemperature(long tick) {
        long timePassed = lastRestorationTick - tick;
        this.lastRestorationTick = tick;
        float energy = this.energy;
        if (timePassed != 0) {
            if (timePassed >= Integer.MAX_VALUE || timePassed < 0) {
                this.energy = 0;
            } else this.energy = temperatureRestorationFunction
                    .restoreTemperature(energy, restorationSpeedFactor, (int) timePassed);
        }
    }

    public TemperatureLogic setTemperatureRestorationFunction(TemperatureRestorationFunction temperatureRestorationFunction) {
        this.temperatureRestorationFunction = temperatureRestorationFunction;
        return this;
    }

    public TemperatureRestorationFunction getTemperatureRestorationFunction() {
        return temperatureRestorationFunction;
    }

    public TemperatureLogic setRestorationSpeedFactor(float restorationSpeedFactor) {
        this.restorationSpeedFactor = restorationSpeedFactor;
        return this;
    }

    public float getRestorationSpeedFactor() {
        return restorationSpeedFactor;
    }

    public TemperatureLogic setTemperatureMaximum(int temperatureMaximum) {
        this.temperatureMaximum = temperatureMaximum;
        return this;
    }

    public int getTemperatureMaximum() {
        return temperatureMaximum;
    }

    public TemperatureLogic setPartialBurnTemperature(@Nullable Integer partialBurnTemperature) {
        this.partialBurnTemperature = partialBurnTemperature;
        return this;
    }

    public @Nullable Integer getPartialBurnTemperature() {
        return partialBurnTemperature;
    }

    public TemperatureLogic setTemperatureMinimum(int temperatureMinimum) {
        this.temperatureMinimum = temperatureMinimum;
        return this;
    }

    public int getTemperatureMinimum() {
        return temperatureMinimum;
    }

    public TemperatureLogic setThermalMass(int thermalMass) {
        this.thermalMass = thermalMass;
        return this;
    }

    public int getThermalMass() {
        return thermalMass;
    }

    @Override
    public @NotNull String getName() {
        return "Temperature";
    }

    @Override
    public void registerToNetLogicData(NetLogicData data) {
        this.owner = data;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("ThermalEnergy", this.energy);
        tag.setInteger("TemperatureMax", this.temperatureMaximum);
        tag.setInteger("TemperatureMin", this.temperatureMinimum);
        tag.setInteger("ThermalMass", this.thermalMass);
        tag.setInteger("RestorationFunction", temperatureRestorationFunction.ordinal());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.energy = nbt.getFloat("ThermalEnergy");
        this.temperatureMaximum = nbt.getInteger("TemperatureMax");
        this.temperatureMinimum = nbt.getInteger("TemperatureMin");
        this.thermalMass = nbt.getInteger("ThermalMass");
        this.temperatureRestorationFunction =
                TemperatureRestorationFunction.values()[nbt.getInteger("RestorationFunction")];
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeFloat(this.energy);
        if (fullChange) {
            buf.writeVarInt(this.temperatureMaximum);
            buf.writeVarInt(this.temperatureMinimum);
            buf.writeVarInt(this.thermalMass);
            buf.writeVarInt(this.temperatureRestorationFunction.ordinal());
        }
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.energy = buf.readFloat();
        if (fullChange) {
            this.temperatureMaximum = buf.readVarInt();
            this.temperatureMinimum = buf.readVarInt();
            this.thermalMass = buf.readVarInt();
            this.temperatureRestorationFunction =
                    TemperatureRestorationFunction.values()[buf.readVarInt()];
        }
    }
}
