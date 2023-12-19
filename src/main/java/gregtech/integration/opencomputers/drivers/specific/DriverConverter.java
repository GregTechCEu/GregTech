package gregtech.integration.opencomputers.drivers.specific;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.converter.MetaTileEntityConverter;
import gregtech.integration.opencomputers.InputValidator;
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

public class DriverConverter extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return MetaTileEntityConverter.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityConverter;
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing enumFacing) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return new EnvironmentTileEntityConverter((IGregTechTileEntity) tileEntity,
                    (MetaTileEntityConverter) ((IGregTechTileEntity) tileEntity).getMetaTileEntity());
        }
        return null;
    }

    public final static class EnvironmentTileEntityConverter extends
                                                             EnvironmentMetaTileEntity<MetaTileEntityConverter> {

        public EnvironmentTileEntityConverter(IGregTechTileEntity holder, MetaTileEntityConverter tileEntity) {
            super(holder, tileEntity, "gt_converter");
        }

        @Callback(doc = "function():number --  Gets the Converter Mode. (0:FE->EU, 1:EU->FE)")
        public Object[] getConverterMode(final Context context, final Arguments args) {
            return new Object[] { tileEntity.isFeToEu() ? 0 : 1 };
        }

        @Callback(doc = "function(mode:number) --  Sets the Converter Mode. (0:FE->EU, 1:EU->FE)")
        public Object[] setConverterMode(final Context context, final Arguments args) {
            boolean mode = InputValidator.getInteger(args, 0, 0, 1) == 0;
            tileEntity.setFeToEu(mode);
            return new Object[] {};
        }
    }
}
