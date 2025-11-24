package gregtech.api.color.containers;

import gregtech.api.color.ColorModeSupport;
import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BedColorContainer extends ColoredBlockContainer {

    public BedColorContainer(@NotNull ResourceLocation id) {
        super(id);
    }

    @Override
    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        // There are no uncolored beds.
        if (newColor == null) {
            return EnumActionResult.FAIL;
        } else if (colorMatches(world, pos, facing, player, newColor)) {
            return EnumActionResult.PASS;
        }

        IBlockState bedPart1 = world.getBlockState(pos);
        BlockBed.EnumPartType partOfBed1 = bedPart1.getValue(BlockBed.PART);
        EnumFacing bedFacing = bedPart1.getValue(BlockHorizontal.FACING);

        // The faced direction is always the direction of the foot -> head.
        BlockPos otherPartPos = pos.offset(partOfBed1 == BlockBed.EnumPartType.FOOT ? bedFacing :
                bedFacing.getOpposite());

        TileEntity bed1TE = world.getTileEntity(pos);
        TileEntity bed2TE = world.getTileEntity(otherPartPos);
        if (!(bed1TE instanceof TileEntityBed bed1 && bed2TE instanceof TileEntityBed bed2)) {
            return EnumActionResult.FAIL;
        }

        bed1.setColor(newColor);
        bed2.setColor(newColor);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        return world.getTileEntity(pos) instanceof TileEntityBed bedTE ? bedTE.getColor() : null;
    }

    @Override
    public boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player) {
        return world.getTileEntity(pos) instanceof TileEntityBed;
    }

    @Override
    public @NotNull ColorModeSupport getSupportedColorMode() {
        return ColorModeSupport.DYE_ONLY;
    }
}
