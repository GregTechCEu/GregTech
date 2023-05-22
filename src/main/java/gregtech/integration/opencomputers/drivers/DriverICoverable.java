package gregtech.integration.opencomputers.drivers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.covers.*;
import gregtech.integration.opencomputers.InputValidator;
import gregtech.integration.opencomputers.values.*;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DriverICoverable extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return ICoverable.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentICoverable((IGregTechTileEntity) tileEntity,
                    tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null));
        }
        return null;
    }

    public final static class EnvironmentICoverable extends EnvironmentMetaTileEntity<ICoverable> {

        public EnvironmentICoverable(IGregTechTileEntity holder, ICoverable capability) {
            super(holder, capability, "gt_coverable");
        }

        @Callback(doc = "function(side:number):table --  Returns cover of side!")
        public Object[] getCover(final Context context, final Arguments args) {
            int index = InputValidator.getInteger(args, 0, 0, 5);
            EnumFacing side = EnumFacing.VALUES[index];
            CoverBehavior coverBehavior = tileEntity.getCoverAtSide(side);
            if (coverBehavior instanceof CoverRoboticArm)
                return new Object[]{new ValueCoverRoboticArm((CoverRoboticArm) coverBehavior, side)};
            if (coverBehavior instanceof CoverConveyor)
                return new Object[]{new ValueCoverConveyor((CoverConveyor) coverBehavior, side)};
            if (coverBehavior instanceof CoverPump)
                return new Object[]{new ValueCoverPump((CoverPump) coverBehavior, side)};
            if (coverBehavior instanceof CoverFluidFilter)
                return new Object[]{new ValueCoverFluidFilter((CoverFluidFilter) coverBehavior, side)};
            if (coverBehavior instanceof CoverItemFilter)
                return new Object[]{new ValueCoverItemFilter((CoverItemFilter) coverBehavior, side)};
            if (coverBehavior instanceof CoverEnderFluidLink) {
                return new Object[]{new ValueCoverEnderFluidLink((CoverEnderFluidLink) coverBehavior, side)};
            }
            if (coverBehavior != null)
                return new Object[]{new ValueCoverBehavior(coverBehavior, side)};
            return new Object[]{null};
        }
    }
}
