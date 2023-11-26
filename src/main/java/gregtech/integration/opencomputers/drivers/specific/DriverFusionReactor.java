package gregtech.integration.opencomputers.drivers.specific;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFusionReactor;
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

public class DriverFusionReactor extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return MetaTileEntityFusionReactor.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityFusionReactor;
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentFusionReactor((IGregTechTileEntity) tileEntity,
                    (MetaTileEntityFusionReactor) ((IGregTechTileEntity) tileEntity).getMetaTileEntity());
        }
        return null;
    }

    public final static class EnvironmentFusionReactor extends
                                                       EnvironmentMetaTileEntity<MetaTileEntityFusionReactor> {

        public EnvironmentFusionReactor(IGregTechTileEntity holder, MetaTileEntityFusionReactor tileEntity) {
            super(holder, tileEntity, "gt_fusionReactor");
        }

        @Callback(doc = "function():number --  Returns the heat of machine.")
        public Object[] getHeat(final Context context, final Arguments args) {
            return new Object[] { tileEntity.getHeat() };
        }
    }
}
