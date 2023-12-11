package gregtech.integration.opencomputers.drivers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
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

public class DriverWorkable extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return IWorkable.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentIWorkable((IGregTechTileEntity) tileEntity,
                    tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side));
        }
        return null;
    }

    public final static class EnvironmentIWorkable extends EnvironmentMetaTileEntity<IWorkable> {

        public EnvironmentIWorkable(IGregTechTileEntity holder, IWorkable capability) {
            super(holder, capability, "gt_workable");
        }

        @Callback(doc = "function():number --  Returns the MaxProgress!")
        public Object[] getMaxProgress(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getMaxProgress() };
        }

        @Callback(doc = "function():number --  Returns the Progress!")
        public Object[] getProgress(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getProgress() };
        }

        @Callback(doc = "function():boolean --  Returns is active or not.")
        public Object[] isActive(final Context context, final Arguments args) {
            return new Object[] { tileEntity.isActive() };
        }

        @Callback(doc = "function():boolean --  Returns is working enabled.")
        public Object[] isWorkingEnabled(final Context context, final Arguments args) {
            return new Object[] { tileEntity.isWorkingEnabled() };
        }

        @Callback(doc = "function(workingEnabled:boolean):boolean -- " +
                "Sets working enabled, return last working enabled.")
        public Object[] setWorkingEnabled(final Context context, final Arguments args) {
            boolean lsatState = tileEntity.isWorkingEnabled();
            tileEntity.setWorkingEnabled(args.checkBoolean(0));
            return new Object[] { lsatState };
        }
    }
}
