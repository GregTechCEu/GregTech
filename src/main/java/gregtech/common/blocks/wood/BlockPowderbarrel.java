package gregtech.common.blocks.wood;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BlockPowderbarrel extends Block {

    public BlockPowderbarrel() {
        super(Material.WOOD);
        setHarvestLevel(ToolClasses.AXE, 1);
        setHardness(0.5f);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public float getExplosionResistance(@NotNull Entity exploder) {
        return 1.0f;
    }

    @Override
    public boolean canBeReplacedByLeaves(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                         @NotNull BlockPos pos) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    // todo
    @Override
    public boolean removedByPlayer(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                   @NotNull EntityPlayer player, boolean willHarvest) {
        if (!world.isRemote) {
            EntityTNTPrimed primed = new EntityTNTPrimed(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    player);
            world.spawnEntity(primed);
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, primed.getSoundCategory(), 1.0f, 1.0f);
            world.setBlockToAir(pos);
            return false;
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    // todo
    @Override
    public void onBlockAdded(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        //if (worldIn.getRedstonePowerFromNeighbors().getIndirectPower(pos) > 0) {
        //    removedByPlayer(state, worldIn, pos, null, false);
        //}
    }
}
