package gregtech.integration.opencomputers.drivers.specific;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import gregtech.integration.opencomputers.drivers.EnvironmentMetaTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public class DriverPowerSubstation extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return MetaTileEntityPowerSubstation.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity gtte) {
            return gtte.getMetaTileEntity() instanceof MetaTileEntityPowerSubstation;
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity gtte &&
                gtte.getMetaTileEntity() instanceof MetaTileEntityPowerSubstation pss) {
            return new EnvironmentPowerSubstation(gtte, pss);
        }
        return null;
    }

    public final static class EnvironmentPowerSubstation extends
                                                         EnvironmentMetaTileEntity<MetaTileEntityPowerSubstation> {

        public EnvironmentPowerSubstation(IGregTechTileEntity holder, MetaTileEntityPowerSubstation mte) {
            super(holder, mte, "gt_powerSubstation");
        }

        @Callback(doc = "function():string -- Returns the stored energy of the machine.")
        public Object[] getStored(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getStored() };
        }

        @Callback(doc = "function():string -- Returns the total energy capacity of the machine.")
        public Object[] getCapacity(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getCapacity() };
        }

        @Callback(doc = "function():number -- Returns the passive drain of the machine, in EU/t.")
        public Object[] getPassiveDrain(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getPassiveDrain() };
        }

        @Callback(doc = "function():number -- Returns the average net EU/t in or out over the last second.")
        public Object[] getAverageIOLastSec(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getAverageIOLastSec() };
        }
    }
}
