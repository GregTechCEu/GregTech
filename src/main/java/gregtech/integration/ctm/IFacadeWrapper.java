package gregtech.integration.ctm;

import gregtech.api.GTValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional;
import team.chisel.ctm.api.IFacade;

import org.jetbrains.annotations.NotNull;

@Optional.Interface(modid = GTValues.MODID_CTM, iface = "team.chisel.ctm.api.IFacade")
public interface IFacadeWrapper extends IFacade {

    @NotNull
    @Override
    IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, EnumFacing side);

    @NotNull
    @Override
    IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, EnumFacing side, @NotNull BlockPos connection);
}
