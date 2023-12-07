package gregtech.common.blocks.wood;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class BlockWoodenDoor extends BlockDoor {

    private final Supplier<ItemStack> itemSupplier;

    public BlockWoodenDoor(Supplier<ItemStack> itemSupplier) {
        super(Material.WOOD);
        this.itemSupplier = itemSupplier;
        setHardness(3);
        setSoundType(SoundType.WOOD);
        disableStats();
        setHarvestLevel(ToolClasses.AXE, 0);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return this.itemSupplier.get();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        if (state.getValue(HALF) == EnumDoorHalf.LOWER) {
            drops.add(itemSupplier.get());
        }
    }
}
