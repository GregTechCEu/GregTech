package gregtech.common.blocks.wood;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.common.entities.PowderbarrelEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockPowderBarrel extends VariantBlock<BlockPowderBarrel.BlockType> {

    public BlockPowderBarrel() {
        super(net.minecraft.block.material.Material.TNT);
        setTranslationKey("powderbarrel");
        setDefaultState(getState(BlockType.POWDERBARREL));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        EntityPlayer player = this.harvesters.get();
        if (!player.isSneaking()) {
            this.explode(worldIn, pos, player);
        } else {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
        }
    }

    public void explode(World worldIn, BlockPos pos, EntityLivingBase igniter) {
        if (!worldIn.isRemote) {
            PowderbarrelEntity powderbarrelEntity = new PowderbarrelEntity(worldIn, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, igniter);
            worldIn.spawnEntity(powderbarrelEntity);
            worldIn.playSound(null, powderbarrelEntity.posX, powderbarrelEntity.posY, powderbarrelEntity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isRemote) {
            PowderbarrelEntity powderbarrelEntity = new PowderbarrelEntity(worldIn, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, explosionIn.getExplosivePlacedBy());
            powderbarrelEntity.setFuse((short) (worldIn.rand.nextInt(powderbarrelEntity.getFuse() / 4) + powderbarrelEntity.getFuse() / 8));
            worldIn.spawnEntity(powderbarrelEntity);
        }
    }

    public enum BlockType implements IStringSerializable, IStateHarvestLevel {

        POWDERBARREL("powderbarrel", 1);

        private final String name;
        private final int harvestLevel;

        BlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }
    }
}
