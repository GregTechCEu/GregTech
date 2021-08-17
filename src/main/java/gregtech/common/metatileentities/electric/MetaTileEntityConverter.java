package gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityConverter extends MetaTileEntity implements ITieredMetaTileEntity {

    public static final float EU_TO_FE = 4;
    public static final float FE_TO_EU = 1 / EU_TO_FE;

    private final int amps;
    private final int tier;
    private final long voltage;
    private final boolean feToEu;

    private final IEnergyStorage energyFE;
    private final IEnergyContainer energyEU;
    private long storedEU;

    private final long capacity;

    private long usedAmps = 0;

    public MetaTileEntityConverter(ResourceLocation metaTileEntityId, int tier, int amps, boolean feToEu) {
        super(metaTileEntityId);
        this.amps = amps;
        this.feToEu = feToEu;
        this.tier = tier;
        this.voltage = GTValues.V[tier];
        this.capacity = this.voltage * 64;
        energyFE = new FEContainer();
        energyEU = new EUContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityConverter(metaTileEntityId, GTUtility.getTierByVoltage(voltage), amps, feToEu);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, pipeline);
        if (feToEu)
            Textures.CONVERTER_FORGE.renderSided(frontFacing, renderState, translation, pipeline);
        else
            Textures.CONVERTER_EU.renderSided(frontFacing, renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        return false;
    }

    private void setEnergyStored(long amount, boolean isEu) {
        if (!isEu)
            amount = (long) (amount * FE_TO_EU);
        if (storedEU != amount) {
            storedEU = amount;
            if (!getWorld().isRemote)
                markDirty();
        }
    }

    private void addEnergy(long amount, boolean isEu) {
        if(!isEu)
            amount *= FE_TO_EU;
        storedEU += amount;
        if (!getWorld().isRemote)
            markDirty();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (!feToEu && (side != frontFacing || side == null))
                    return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(energyEU);
            else if (feToEu && side == frontFacing)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(energyEU);
            return null;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            if (feToEu && (side != frontFacing || side == null))
                return CapabilityEnergy.ENERGY.cast(energyFE);
            else if (!feToEu && side == frontFacing)
                return CapabilityEnergy.ENERGY.cast(energyFE);
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void update() {
        super.update();
        usedAmps = 0;
        if (getWorld().isRemote) return;
        TileEntity tile = getWorld().getTileEntity(getPos().offset(frontFacing));
        if (tile == null) return;
        EnumFacing opposite = frontFacing.getOpposite();
        if (feToEu) {
            IEnergyContainer container = tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if (container != null) {
                long ampsUsed = container.acceptEnergyFromNetwork(opposite, voltage, Math.min(amps, storedEU / voltage));
                energyEU.changeEnergy(voltage * ampsUsed);
            }
        } else {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, opposite);
            if (storage != null) {
                int inserted = storage.receiveEnergy((int) (Math.min(storedEU, voltage * amps) * EU_TO_FE), false);
                energyEU.changeEnergy((long) (-inserted * FE_TO_EU));
            }
        }
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if(feToEu) {
            tooltip.add(I18n.format("gregtech.machine.converter_fe.tooltip"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", voltage, GTValues.VN[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out", amps));
        } else {
            tooltip.add(I18n.format("gregtech.machine.converter_eu.tooltip"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", voltage, GTValues.VN[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in", amps));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", capacity));
    }

    // -- GTCE Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            if(feToEu || usedAmps >= amps) return 0;
            long canAccept = capacity - storedEU;
            if (voltage > 0L && amperage > 0L && (side == null || inputsEnergy(side))) {
                if (voltage > getInputVoltage()) {
                    GTUtility.doOvervoltageExplosion(MetaTileEntityConverter.this, voltage);
                    return Math.min(amperage, getInputAmperage());
                }
                if (canAccept >= voltage) {
                    long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, amps - usedAmps));
                    if (amperesAccepted > 0) {
                        setEnergyStored(storedEU + voltage * amperesAccepted, true);
                        usedAmps += amperesAccepted;
                        return amperesAccepted;
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return !feToEu && side != getFrontFacing();
        }

        @Override
        public long changeEnergy(long amount) {
            long old = storedEU;
            long energy = storedEU + amount;
            if(energy > capacity)
                energy = capacity;
            else if(energy < 0)
                energy = 0;

            setEnergyStored(energy, true);
            return energy - old;
        }

        @Override
        public long getEnergyStored() {
            return storedEU;
        }

        @Override
        public long getEnergyCapacity() {
            return capacity;
        }

        @Override
        public long getInputAmperage() {
            return feToEu ? 0 : amps;
        }

        @Override
        public long getInputVoltage() {
            return feToEu ? 0 : voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? amps : 0;
        }

        @Override
        public long getOutputVoltage() {
            return feToEu ? voltage : 0;
        }
    }

    // -- GTCE Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if(!feToEu) return 0;
            int energyReceived = (int) Math.min(capacity - storedEU, Math.min(voltage, maxReceive * FE_TO_EU));
            if (!simulate) {
                addEnergy(energyReceived, true);
            }

            return (int) (energyReceived * EU_TO_FE);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = (int) Math.min(storedEU, Math.min(voltage, maxExtract * FE_TO_EU));
            if (!simulate) {
                addEnergy(-maxExtract, true);
            }

            return (int) (energyExtracted * EU_TO_FE);
        }

        @Override
        public int getEnergyStored() {
            return (int) (storedEU * EU_TO_FE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) (capacity * EU_TO_FE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return feToEu;
        }
    }
    /*






     */
}
