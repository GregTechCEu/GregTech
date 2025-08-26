package gregtech.common.blocks.wood;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BlockRubberDoor extends BlockWoodenDoor {

    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0, 0, 0, 1, 1, 2 / 16.0);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0, 0, 14 / 16.0, 1, 1, 1);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(14 / 16.0, 0, 0, 1, 1, 1);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0, 0, 0, 2 / 16.0, 1, 1);

    public BlockRubberDoor(Supplier<ItemStack> itemSupplier) {
        super(itemSupplier);
        setHarvestLevel(ToolClasses.AXE, 0);
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source,
                                                 @NotNull BlockPos pos) {
        state = state.getActualState(source, pos);
        EnumFacing facing = state.getValue(FACING);
        boolean open = !state.getValue(OPEN);
        boolean rh = state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;

        return switch (facing) {
            case SOUTH -> open ? SOUTH_AABB : (rh ? EAST_AABB : WEST_AABB);
            case WEST -> open ? WEST_AABB : (rh ? SOUTH_AABB : NORTH_AABB);
            case NORTH -> open ? NORTH_AABB : (rh ? WEST_AABB : EAST_AABB);
            default -> open ? EAST_AABB : (rh ? NORTH_AABB : SOUTH_AABB);
        };
    }
}
