package gregtech.integration.opencomputers.drivers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.covers.*;
import gregtech.integration.opencomputers.InputValidator;
import gregtech.integration.opencomputers.values.*;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public class DriverCoverHolder extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return CoverHolder.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentICoverable((IGregTechTileEntity) tileEntity,
                    tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, null));
        }
        return null;
    }

    public final static class EnvironmentICoverable extends EnvironmentMetaTileEntity<CoverHolder> {

        public EnvironmentICoverable(IGregTechTileEntity holder, CoverHolder capability) {
            super(holder, capability, "gt_coverable");
        }

        @Callback(doc = "function(side:number):table --  Returns cover of side!")
        public Object[] getCover(final Context context, final Arguments args) {
            int index = InputValidator.getInteger(args, 0, 0, 5);
            EnumFacing side = EnumFacing.VALUES[index];
            Cover cover = tileEntity.getCoverAtSide(side);
            if (cover instanceof CoverRoboticArm robotArm)
                return new Object[] { new ValueCoverRoboticArm(robotArm, side) };
            if (cover instanceof CoverConveyor conveyor)
                return new Object[] { new ValueCoverConveyor(conveyor, side) };
            if (cover instanceof CoverFluidRegulator regulator)
                return new Object[] { new ValueCoverFluidRegulator(regulator, side) };
            if (cover instanceof CoverPump pump)
                return new Object[] { new ValueCoverPump(pump, side) };
            if (cover instanceof CoverFluidFilter filter)
                return new Object[] { new ValueCoverFluidFilter(filter, side) };
            if (cover instanceof CoverItemFilter filter)
                return new Object[] { new ValueCoverItemFilter(filter, side) };
            if (cover instanceof CoverEnderFluidLink efl)
                return new Object[] { new ValueCoverEnderFluidLink(efl, side) };
            if (cover != null)
                return new Object[] { new ValueCoverBehavior(cover, side) };
            return new Object[] { null };
        }
    }
}
