package gregtech.integration.ctm;

import gregtech.api.GTValues;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional;

import team.chisel.ctm.api.IFacade;

import javax.annotation.Nonnull;

@Optional.Interface(modid = GTValues.MODID_CTM, iface = "team.chisel.ctm.api.IFacade")
public interface IFacadeWrapper extends IFacade {

    @Nonnull
    @Override
    IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side);

    @Nonnull
    @Override
    IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side,
                          @Nonnull BlockPos connection);
}
