package gregtech.integration.opencomputers.drivers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public class DriverEnergyContainer extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return IEnergyContainer.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentIEnergyContainer((IGregTechTileEntity) tileEntity,
                    tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side));
        }
        return null;
    }

    public static final class EnvironmentIEnergyContainer extends EnvironmentMetaTileEntity<IEnergyContainer> {

        public EnvironmentIEnergyContainer(IGregTechTileEntity holder, IEnergyContainer capability) {
            super(holder, capability, "gt_energyContainer");
        }

        @Callback(doc = "function():number --  Returns the amount of electricity contained in this Block, in EU units!")
        public Object[] getEnergyStored(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getEnergyStored() };
        }

        @Callback(doc = "function():number --  " +
                "Returns the amount of electricity containable in this Block, in EU units!")
        public Object[] getEnergyCapacity(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getEnergyCapacity() };
        }

        @Callback(doc = "function():number --  Gets the Output in EU/p.")
        public Object[] getOutputVoltage(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getOutputVoltage() };
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getOutputAmperage(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getOutputAmperage() };
        }

        @Callback(doc = "function():number -- Gets the maximum Input in EU/p.")
        public Object[] getInputVoltage(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getInputVoltage() };
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getInputAmperage(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getInputAmperage() };
        }

        @Callback(doc = "function():number -- Gets the energy input per second.")
        public Object[] getInputPerSec(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getInputPerSec() };
        }

        @Callback(doc = "function():number -- Gets the energy output per second.")
        public Object[] getOutputPerSec(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getOutputPerSec() };
        }
    }
}
