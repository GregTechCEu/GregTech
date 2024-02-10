package gregtech.common.blocks.explosive;

import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.entities.EntityGTExplosive;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public abstract class BlockGTExplosive extends Block {

    private final boolean canRedstoneActivate;
    private final boolean explodeOnMine;
    private final int fuseLength;

    /**
     * @param canRedstoneActivate whether redstone signal can prime this explosive
     * @param explodeOnMine       whether mining this block should prime it (sneak mine to drop normally)
     * @param fuseLength          explosion countdown after priming. Vanilla TNT is 80.
     */
    public BlockGTExplosive(Material materialIn, boolean canRedstoneActivate, boolean explodeOnMine, int fuseLength) {
        super(materialIn);
        this.canRedstoneActivate = canRedstoneActivate;
        this.explodeOnMine = explodeOnMine;
        this.fuseLength = fuseLength;
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_TOOLS);
    }

    protected abstract EntityGTExplosive createEntity(World world, BlockPos pos, EntityLivingBase exploder);

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
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(@NotNull Explosion explosion) {
        return false;
    }

    public void explode(World world, BlockPos pos, EntityLivingBase exploder) {
        if (!world.isRemote) {
            EntityGTExplosive entity = createEntity(world, pos, exploder);
            entity.setFuse(fuseLength);
            world.spawnEntity(entity);
            world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public void onExplosionDestroy(@NotNull World world, @NotNull BlockPos pos, @NotNull Explosion explosion) {
        if (!world.isRemote) {
            EntityGTExplosive entity = createEntity(world, pos, explosion.getExplosivePlacedBy());
            entity.setFuse(world.rand.nextInt(fuseLength / 4) + fuseLength / 8);
            world.spawnEntity(entity);
        }
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE)) {
            this.explode(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            if (stack.getItem() == Items.FLINT_AND_STEEL) {
                stack.damageItem(1, player);
            } else if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void dropBlockAsItemWithChance(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                          float chance, int fortune) {
        if (explodeOnMine) {
            EntityPlayer player = this.harvesters.get();
            if (!player.isSneaking()) {
                this.explode(world, pos, player);
                return;
            }
        }
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    @Override
    public void onEntityCollision(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entity) {
        if (!world.isRemote && entity instanceof EntityArrow arrow) {
            if (arrow.isBurning()) {
                this.explode(world, pos, arrow.shootingEntity instanceof EntityLivingBase living ? living : null);
                world.setBlockToAir(pos);
            }
        }
    }

    @Override
    public void onBlockAdded(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (canRedstoneActivate) {
            if (world.isBlockPowered(pos)) {
                explode(world, pos, null);
                world.setBlockToAir(pos);
            }
        }
    }

    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos fromPos) {
        if (canRedstoneActivate) {
            if (world.isBlockPowered(pos)) {
                explode(world, pos, null);
                world.setBlockToAir(pos);
            }
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (explodeOnMine) {
            tooltip.add(I18n.format("tile.gt_explosive.breaking_tooltip"));
        }
        if (!canRedstoneActivate) {
            tooltip.add(I18n.format("tile.gt_explosive.lighting_tooltip"));
        }
    }
}
