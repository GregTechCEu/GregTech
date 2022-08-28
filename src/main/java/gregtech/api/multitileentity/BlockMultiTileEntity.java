package gregtech.api.multitileentity;

import codechicken.lib.texture.TextureUtils;
import com.google.common.base.Predicate;
import gregtech.api.GTValues;
import gregtech.api.block.BlockCustomParticle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockMultiTileEntity extends BlockCustomParticle {

    private static final Map<String, BlockMultiTileEntity> MTE_BLOCKS = new Object2ObjectOpenHashMap<>();

    private static boolean LOCK = false;

    private final int harvestLevel;
    private final String internalName;
    private final String harvestTool;
    private final boolean isOpaque;
    private final boolean isNormalCube;

    public MapColor mapColor = null;

    /**
     * @param modID               the modid of the block
     * @param vanillaMaterialName the Name of the Vanilla {@link Material}. In case this is not a vanilla Material, insert the Name you want to give your own Material instead.
     * @param vanillaMaterial     the Material used to determine the Block's attributes.
     * @param soundType           the {@link  SoundType} of the Block.
     * @param harvestTool         the Tool used to harvest this Block. Must be all lowercase.
     * @param harvestLevel        the harvest level required to mine this block. Must not be less than 0.
     * @param isOpaque            if this Block is Opaque.
     * @param isNormalCube        if this Block is a normal Cube. Used for Redstone Stuff.
     */
    private BlockMultiTileEntity(@Nonnull String modID, @Nonnull String vanillaMaterialName, @Nonnull Material vanillaMaterial, @Nonnull SoundType soundType, @Nonnull String harvestTool, int harvestLevel, boolean isOpaque, boolean isNormalCube) {
        super(vanillaMaterial);
        this.internalName = getName(vanillaMaterialName, soundType, harvestTool, harvestLevel, isOpaque, isNormalCube);

        MTE_BLOCKS.put(modID + ":" + this.internalName, this);

        setSoundType(soundType);
        this.isOpaque = isOpaque;
        this.isNormalCube = isNormalCube;

        this.harvestTool = harvestTool.toLowerCase();
        this.harvestLevel = Math.max(0, harvestLevel);

        if (Loader.isModLoaded(GTValues.MODID_MEK)) {
            //TODO Mekanism Carboard Box Blacklisting
//            try {
//                MekanismAPI.addBoxBlacklist(this, GTValues.W);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
        }
    }

    @Nonnull
    public static String getName(@Nonnull String vanillaMaterial, @Nonnull SoundType soundType, @Nonnull String harvestTool, int harvestLevel, boolean isOpaque, boolean isNormalCube) {
        return "gt.block.multitileentity." + vanillaMaterial + "." + soundType.getBreakSound().getSoundName() + "." + harvestTool.toLowerCase() + "." + harvestLevel + "." + isOpaque + "." + isNormalCube;
    }

    /**
     * @param modID               the modid of the block
     * @param vanillaMaterialName the Name of the Vanilla {@link Material}. In case this is not a vanilla Material, insert the Name you want to give your own Material instead.
     * @param vanillaMaterial     the Material used to determine the Block's attributes.
     * @param soundType           the {@link  SoundType} of the Block.
     * @param harvestTool         the Tool used to harvest this Block. Must be all lowercase.
     * @param harvestLevel        the harvest level required to mine this block. Must not be less than 0.
     * @param isOpaque            if this Block is Opaque.
     * @param isNormalCube        if this Block is a normal Cube. Used for Redstone Stuff.
     */
    @Nonnull
    public static BlockMultiTileEntity getOrCreate(@Nonnull String modID, @Nonnull String vanillaMaterialName, @Nonnull Material vanillaMaterial, @Nonnull SoundType soundType, @Nonnull String harvestTool, int harvestLevel, boolean isOpaque, boolean isNormalCube) {
        BlockMultiTileEntity rBlock = MTE_BLOCKS.get(modID + ":" + getName(vanillaMaterialName, soundType, harvestTool, harvestLevel, isOpaque, isNormalCube));
        return rBlock == null ? new BlockMultiTileEntity(modID, vanillaMaterialName, vanillaMaterial, soundType, harvestTool, harvestLevel, isOpaque, isNormalCube) : rBlock;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return this.isOpaque;
    }

    @Override
    public int getLightOpacity(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        return isOpaqueCube(state) ? 255 : 0;
    }

    @Override
    public final void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (this.hasTileEntity(state)) {
            worldIn.removeTileEntity(pos);
        }
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return this.mapColor == null ? super.getMapColor(state, worldIn, pos) : this.mapColor;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsSideSolid) {
            return ((IMultiTileEntity.IMTE_IsSideSolid) tileEntity).isSideSolid(side);
        }
        return this.isOpaque;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanCreatureSpawn) {
            return ((IMultiTileEntity.IMTE_CanCreatureSpawn) tileEntity).canCreatureSpawn(type);
        }
        return false;
    }

    @Override
    public boolean isPassable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_BlocksMovement) {
            return !((IMultiTileEntity.IMTE_BlocksMovement) tileEntity).blocksMovement();
        }
        return false;
    }

    @Override
    public boolean isReplaceable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsReplaceable) {
            return !((IMultiTileEntity.IMTE_IsReplaceable) tileEntity).isReplaceable();
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBlockHardness(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetBlockHardness) {
            return ((IMultiTileEntity.IMTE_GetBlockHardness) tileEntity).getBlockHardness();
        }
        return 1.0F;
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity tileEntity = source.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetBoundingBox) {
            return ((IMultiTileEntity.IMTE_GetBoundingBox) tileEntity).getBoundingBox();
        }
        return Block.FULL_BLOCK_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getPackedLightmapCoords(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        TileEntity tileEntity = source.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetPackedLightmapCoords) {
            return ((IMultiTileEntity.IMTE_GetPackedLightmapCoords) tileEntity).getPackedLightmapCoords();
        }
        return source.getCombinedLight(pos, state.getLightValue(source, pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_ShouldSideBeRendered) {
            return ((IMultiTileEntity.IMTE_ShouldSideBeRendered) tileEntity).shouldSideBeRendered(side);
        }
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetBlockFaceShape) {
            return ((IMultiTileEntity.IMTE_GetBlockFaceShape) tileEntity).getBlockFaceShape(face);
        }
        return BlockFaceShape.SOLID;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_AddCollisionBoxesToList) {
            ((IMultiTileEntity.IMTE_AddCollisionBoxesToList) tileEntity).addCollisionBoxesToList(entityBox, collidingBoxes, entityIn);
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetCollisionBoundingBox) {
            return ((IMultiTileEntity.IMTE_GetCollisionBoundingBox) tileEntity).getCollisionBoundingBox();
        }
        return super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        AxisAlignedBB aabb = getCollisionBoundingBox(state, worldIn, pos);
        return aabb == null ? super.getSelectedBoundingBox(state, worldIn, pos) : aabb.offset(pos);
    }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_RandomTick) {
            ((IMultiTileEntity.IMTE_RandomTick) tileEntity).randomTick(random);
        }
    }

    @Override
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_UpdateTick) {
            ((IMultiTileEntity.IMTE_UpdateTick) tileEntity).updateTick(rand);
        }
    }

    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_RandomDisplayTick) {
            ((IMultiTileEntity.IMTE_RandomDisplayTick) tileEntity).randomDisplayTick(rand);
        } else {
            super.randomDisplayTick(stateIn, worldIn, pos, rand);
        }
    }

    @Override
    public void onPlayerDestroy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnPlayerDestroy) {
            ((IMultiTileEntity.IMTE_OnPlayerDestroy) tileEntity).onPlayerDestroy();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        onNeighborChange(worldIn, pos, fromPos);
    }

    @Override
    public void onNeighborChange(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull BlockPos neighbor) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!LOCK) {
            LOCK = true;
            if (tileEntity instanceof IMultiTileEntity)
                ((IMultiTileEntity) tileEntity).onAdjacentBlockChange(pos);
            LOCK = false;
        }
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnNeighborChanged) {
            ((IMultiTileEntity.IMTE_OnNeighborChanged) tileEntity).onNeighborChanged(world, pos);
        }
    }

    //TODO What is this
//    @Override
//    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
//        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
//    }

    //TODO What is this
//    @Override
//    public int tickRate(World worldIn) {
//        return super.tickRate(worldIn);
//    }

    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnBlockAdded) {
            ((IMultiTileEntity.IMTE_OnBlockAdded) tileEntity).onBlockAdded();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getPlayerRelativeBlockHardness(@Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetPlayerRelativeBlockHardness) {
            return ((IMultiTileEntity.IMTE_GetPlayerRelativeBlockHardness) tileEntity).getPlayerRelativeBlockHardness(player, super.getPlayerRelativeBlockHardness(state, player, worldIn, pos));
        }
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetDrops) {
            NonNullList<ItemStack> list = ((IMultiTileEntity.IMTE_GetDrops) tileEntity).getDrops(fortune, false);
            chance = ForgeEventFactory.fireBlockHarvesting(list, worldIn, pos, state, fortune, chance, false, this.harvesters.get());
            for (ItemStack stack : list) {
                if (GTValues.RNG.nextFloat() <= chance) {
                    spawnAsEntity(worldIn, pos, stack);
                }
            }
        }
    }

    @Override
    public void dropXpOnBlockBreak(@Nonnull World worldIn, @Nonnull BlockPos pos, int amount) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_DropXpOnBlockBreak) {
            ((IMultiTileEntity.IMTE_DropXpOnBlockBreak) tileEntity).dropXpOnBlockBreak(amount);
        } else {
            super.dropXpOnBlockBreak(worldIn, pos, amount);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CollisionRayTrace) {
            return ((IMultiTileEntity.IMTE_CollisionRayTrace) tileEntity).collisionRayTrace(pos, start, end);
        } else {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
    }

    @Override
    public void onExplosionDestroy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Explosion explosionIn) {
        onBlockExploded(worldIn, pos, explosionIn);
    }

    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanPlaceBlockAt) {
            return ((IMultiTileEntity.IMTE_CanPlaceBlockAt) tileEntity).canPlaceBlockOnSide(pos, side);
        }
        return this.isReplaceable(worldIn, pos);
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanPlaceBlockAt) {
            return ((IMultiTileEntity.IMTE_CanPlaceBlockAt) tileEntity).canPlaceBlockAt(pos);
        }
        return this.isReplaceable(worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnBlockActivated) {
            return ((IMultiTileEntity.IMTE_OnBlockActivated) tileEntity).onBlockActivated(playerIn, playerIn.getHeldItem(hand), facing, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public void onEntityWalk(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnEntityWalk) {
            ((IMultiTileEntity.IMTE_OnEntityWalk) tileEntity).onEntityWalk(entityIn);
        }
    }

    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnBlockClicked) {
            ((IMultiTileEntity.IMTE_OnBlockClicked) tileEntity).onBlockClicked(playerIn);
        }
    }

    @Nonnull
    @Override
    public Vec3d modifyAcceleration(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn, @Nonnull Vec3d motion) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_ModifyAcceleration) {
            return ((IMultiTileEntity.IMTE_ModifyAcceleration) tileEntity).modifyAcceleration(entityIn, motion);
        }
        return super.modifyAcceleration(worldIn, pos, entityIn, motion);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetWeakPower) {
            return ((IMultiTileEntity.IMTE_GetWeakPower) tileEntity).getWeakPower(side);
        }
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnEntityCollision) {
            ((IMultiTileEntity.IMTE_OnEntityCollision) tileEntity).onEntityCollision(entityIn);
        } else {
            super.onEntityCollision(worldIn, pos, state, entityIn);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getStrongPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = blockAccess.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetStrongPower) {
            return ((IMultiTileEntity.IMTE_GetStrongPower) tileEntity).getStrongPower(side);
        }
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        // vanilla does not check this for null, so neither do we
        //noinspection ConstantConditions
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F); // 1.7 does 0.025F instead...

        boolean isSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetDrops) {
            NonNullList<ItemStack> list = ((IMultiTileEntity.IMTE_GetDrops) tileEntity).getDrops(fortune, isSilkTouch);
            float chance = ForgeEventFactory.fireBlockHarvesting(list, worldIn, pos, state, fortune, 1.0F, isSilkTouch, player);
            for (ItemStack itemStack : list) {
                if (GTValues.RNG.nextFloat() <= chance) {
                    spawnAsEntity(worldIn, pos, itemStack);
                }
            }
        }
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnPlacedBy) {
            ((IMultiTileEntity.IMTE_OnPlacedBy) tileEntity).onBlockPlacedBy(placer, stack);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean eventReceived(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, int id, int param) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity == null || tileEntity.receiveClientEvent(id, param);
    }

    @Override
    public void onFallenUpon(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn, float fallDistance) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnFallenUpon) {
            ((IMultiTileEntity.IMTE_OnFallenUpon) tileEntity).onFallenUpon(entityIn, fallDistance);
        } else {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetStackFromBlock) {
            return ((IMultiTileEntity.IMTE_GetStackFromBlock) tileEntity).getStackFromBlock();
        } else {
            return super.getItem(worldIn, pos, state);
        }
    }

    @Override
    public void onBlockHarvested(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnBlockHarvested) {
            ((IMultiTileEntity.IMTE_OnBlockHarvested) tileEntity).onBlockHarvested(player);
        } else {
            super.onBlockHarvested(worldIn, pos, state, player);
        }
    }

    @Override
    public void fillWithRain(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_FillWithRain) {
            ((IMultiTileEntity.IMTE_FillWithRain) tileEntity).fillWithRain();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetComparatorInputOverride) {
            return ((IMultiTileEntity.IMTE_GetComparatorInputOverride) tileEntity).getComparatorInputOverride();
        }
        return super.getComparatorInputOverride(blockState, worldIn, pos);
    }

    //TODO What is this
//    @Override
//    public Vec3d getOffset(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
//        return super.getOffset(state, worldIn, pos);
//    }

    @Override
    public float getSlipperiness(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetSlipperiness) {
            return ((IMultiTileEntity.IMTE_GetSlipperiness) tileEntity).getSlipperiness(entity);
        }
        return super.getSlipperiness(state, world, pos, entity);
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetLightValue) {
            return ((IMultiTileEntity.IMTE_GetLightValue) tileEntity).getLightValue();
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public boolean isLadder(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entity) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsLadder) {
            return ((IMultiTileEntity.IMTE_IsLadder) tileEntity).isLadder(entity);
        }
        return super.isLadder(state, world, pos, entity);
    }

    @Override
    public boolean isNormalCube(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsNormalCube) {
            return ((IMultiTileEntity.IMTE_IsNormalCube) tileEntity).isNormalCube();
        }
        return this.isNormalCube;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_DoesSideBlockRendering) {
            return ((IMultiTileEntity.IMTE_DoesSideBlockRendering) tileEntity).doesSideBlockRendering(face);
        }
        return super.doesSideBlockRendering(state, world, pos, face);
    }

    @Override
    public boolean isBurning(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsBurning) {
            return ((IMultiTileEntity.IMTE_IsBurning) tileEntity).isBurning();
        }
        return false;
    }

    @Override
    public boolean isAir(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsAir) {
            return ((IMultiTileEntity.IMTE_IsAir) tileEntity).isAir();
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_RemovedByPlayer) {
            return ((IMultiTileEntity.IMTE_RemovedByPlayer) tileEntity).removedByPlayer(world, player, willHarvest);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getFlammability(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        TileEntity tileEntity = world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetFlammability) {
            return ((IMultiTileEntity.IMTE_GetFlammability) tileEntity).getFlammability(face, getMaterial(state).getCanBurn());
        }
        return getMaterial(state).getCanBurn() ? 150 : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getFireSpreadSpeed(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        TileEntity tileEntity = world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetFireSpreadSpeed) {
            return ((IMultiTileEntity.IMTE_GetFireSpreadSpeed) tileEntity).getFireSpreadSpeed(face, getMaterial(state).getCanBurn());
        }
        return getMaterial(state).getCanBurn() ? 150 : 0;
    }

    @Override
    public boolean isFireSource(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsFireSource) {
            return ((IMultiTileEntity.IMTE_IsFireSource) tileEntity).isFireSource(side);
        }
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        // we create the TE elsewhere, do not fret
        return null;
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {/**/}

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetDrops) {
            drops.addAll(((IMultiTileEntity.IMTE_GetDrops) tileEntity).getDrops(fortune, false));
        }
    }

    @Override
    public final boolean canSilkHarvest(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isBed(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Entity player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Bed) {
            return ((IMultiTileEntity.IMTE_Bed) tileEntity).isBed(player);
        }
        return false;
    }

    @Nullable
    @Override
    public BlockPos getBedSpawnPosition(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Bed) {
            return ((IMultiTileEntity.IMTE_Bed) tileEntity).getBedSpawnPosition(player);
        }
        return BlockBed.getSafeExitLocation((World) world, pos, 0);
    }

    @Override
    public void setBedOccupied(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean occupied) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Bed) {
            ((IMultiTileEntity.IMTE_Bed) tileEntity).setBedOccupied(player, occupied);
        }
    }

    @Nonnull
    @Override
    public EnumFacing getBedDirection(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Bed) {
            return ((IMultiTileEntity.IMTE_Bed) tileEntity).getBedDirection();
        }
        return EnumFacing.NORTH;
    }

    @Override
    public boolean isBedFoot(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Bed) {
            return ((IMultiTileEntity.IMTE_Bed) tileEntity).isBedFoot();
        }
        return false;
    }

    @Override
    public void beginLeavesDecay(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Leaves) {
            ((IMultiTileEntity.IMTE_Leaves) tileEntity).beginLeavesDecay();
        }
    }

    @Override
    public boolean canSustainLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Leaves) {
            return ((IMultiTileEntity.IMTE_Leaves) tileEntity).canSustainLeaves();
        }
        return false;
    }

    @Override
    public boolean isLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Leaves) {
            return ((IMultiTileEntity.IMTE_Leaves) tileEntity).isLeaves();
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanBeReplacedByLeaves) {
            return ((IMultiTileEntity.IMTE_CanBeReplacedByLeaves) tileEntity).canBeReplacedByLeaves();
        }
        return false;
    }

    @Override
    public boolean isWood(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsWood) {
            return ((IMultiTileEntity.IMTE_IsWood) tileEntity).isWood();
        }
        return false;
    }

    @Override
    public boolean isReplaceableOreGen(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Predicate<IBlockState> target) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsReplaceableOregen) {
            return ((IMultiTileEntity.IMTE_IsReplaceableOregen) tileEntity).isReplaceableOregen(target);
        }
        return false;
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetExplosionResistance) {
            return ((IMultiTileEntity.IMTE_GetExplosionResistance) tileEntity).getExplosionResistance(exploder, pos);
        }
        return 1.0F;
    }

    @Override
    public void onBlockExploded(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnBlockExploded) {
            ((IMultiTileEntity.IMTE_OnBlockExploded) tileEntity).onBlockExploded(explosion);
        } else {
            super.onExplosionDestroy(world, pos, explosion);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanConnectRedstone) {
            return ((IMultiTileEntity.IMTE_CanConnectRedstone) tileEntity).canConnectRedstone(side);
        }
        return super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean canPlaceTorchOnTop(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanPlaceTorchOnTop) {
            return ((IMultiTileEntity.IMTE_CanPlaceTorchOnTop) tileEntity).canPlaceTorchOnTop();
        }
        return isSideSolid(state, world, pos, EnumFacing.UP);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetPickBlock) {
            return ((IMultiTileEntity.IMTE_GetPickBlock) tileEntity).getPickBlock(target);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isFoliage(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsFoliage) {
            return ((IMultiTileEntity.IMTE_IsFoliage) tileEntity).isFoliage();
        }
        return false;
    }

    @Override
    public boolean canSustainPlant(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing direction, @Nonnull IPlantable plantable) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanSustainPlant) {
            return ((IMultiTileEntity.IMTE_CanSustainPlant) tileEntity).canSustainPlant(direction, plantable);
        }
        return false;
    }

    @Override
    public void onPlantGrow(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockPos source) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_OnPlantGrow) {
            ((IMultiTileEntity.IMTE_OnPlantGrow) tileEntity).onPlantGrow(source);
        }
    }

    @Override
    public boolean isFertile(@Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsFertile) {
            return ((IMultiTileEntity.IMTE_IsFertile) tileEntity).isFertile();
        }
        return false;
    }

    @Override
    public boolean canEntityDestroy(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_CanEntityDestroy) {
            return ((IMultiTileEntity.IMTE_CanEntityDestroy) tileEntity).canEntityDestroy(entity);
        }
        return true;
    }

    @Override
    public boolean isBeaconBase(@Nonnull IBlockAccess worldObj, @Nonnull BlockPos pos, @Nonnull BlockPos beacon) {
        TileEntity tileEntity = worldObj.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_IsBeaconBase) {
            return ((IMultiTileEntity.IMTE_IsBeaconBase) tileEntity).isBeaconBase();
        }
        return false;
    }

    @Override
    public boolean rotateBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Rotateable) {
            return ((IMultiTileEntity.IMTE_Rotateable) tileEntity).rotateBlock(axis);
        }
        return false;
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(@Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_Rotateable) {
            return ((IMultiTileEntity.IMTE_Rotateable) tileEntity).getValidRotations();
        }
        return new EnumFacing[0];
    }

    @Override
    public float getEnchantPowerBonus(@Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetEnchantPowerBonus) {
            return ((IMultiTileEntity.IMTE_GetEnchantPowerBonus) tileEntity).getEnchantPowerBonus();
        }
        return 0.0F;
    }

    @Override
    public boolean recolorBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EnumDyeColor color) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_RecolorBlock) {
            return ((IMultiTileEntity.IMTE_RecolorBlock) tileEntity).recolorBlock(side, color);
        }
        return false;
    }

    @Override
    public int getExpDrop(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, int fortune) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_DropXpOnBlockBreak) {
            return ((IMultiTileEntity.IMTE_DropXpOnBlockBreak) tileEntity).getXpDropped(fortune);
        }
        return 0;
    }

    //TODO What is this
//    @Override
//    public void observedNeighborChange(@Nonnull IBlockState observerState, @Nonnull World world, @Nonnull BlockPos observerPos, @Nonnull Block changedBlock, @Nonnull BlockPos changedBlockPos) {
//        super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos);
//    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_ShouldCheckWeakPower) {
            return ((IMultiTileEntity.IMTE_ShouldCheckWeakPower) tileEntity).shouldCheckWeakPower(side);
        }
        return false;
    }

    @Override
    public boolean getWeakChanges(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetWeakChanges) {
            return ((IMultiTileEntity.IMTE_GetWeakChanges) tileEntity).getWeakChanges();
        }
        return false;
    }

    //TODO What is this
//    @Override
//    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
//        return super.getExtendedState(state, world, pos);
//    }

    //TODO What is this
//    @Nullable
//    @Override
//    public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {
//        return super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
//    }

    //TODO What is this
//    @Nullable
//    @Override
//    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
//        return super.isAABBInsideMaterial(world, pos, boundingBox, materialIn);
//    }

    //TODO What is this
//    @Nullable
//    @Override
//    public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
//        return super.isAABBInsideLiquid(world, pos, boundingBox);
//    }

    @Nonnull
    @Override
    public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetSoundType) {
            return ((IMultiTileEntity.IMTE_GetSoundType) tileEntity).getSoundType(entity);
        }
        return this.blockSoundType;
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockPos beaconPos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetBeaconColorMultiplier) {
            return ((IMultiTileEntity.IMTE_GetBeaconColorMultiplier) tileEntity).getBeaconColorMultiplier(beaconPos);
        }
        return null;
    }

    @Nonnull
    @Override
    public Vec3d getFogColor(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entity, @Nonnull Vec3d originalColor, float partialTicks) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetFogColor) {
            return ((IMultiTileEntity.IMTE_GetFogColor) tileEntity).getFogColor(entity, originalColor, partialTicks);
        }
        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(@Nonnull World world, @Nonnull BlockPos blockPos) {
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity instanceof IMultiTileEntity.IMTE_GetParticleTexture) {
            return ((IMultiTileEntity.IMTE_GetParticleTexture) tileEntity).getParticleTexture();
        }
        return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
    }

    //TODO What is this
//    @Override
//    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
//        return super.getStateAtViewpoint(state, world, pos, viewpoint);
//    }

    //TODO What is this
//    @Override
//    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
//        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
//    }

    //TODO What is this
//    @Override
//    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
//        return super.canBeConnectedTo(world, pos, facing);
//    }

    //TODO What is this
//    @Nullable
//    @Override
//    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
//        return super.getAiPathNodeType(state, world, pos);
//    }

    //TODO What is this
//    @Nullable
//    @Override
//    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EntityLiving entity) {
//        return super.getAiPathNodeType(state, world, pos, entity);
//    }


    //TODO What is this
//    @Override
//    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
//        return super.getStateAtViewpoint(state, world, pos, viewpoint);
//    }

    //TODO What is this
//    @Override
//    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
//        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
//    }

    @Override
    public int getHarvestLevel(@Nonnull IBlockState state) {
        return this.harvestLevel;
    }

    @Nonnull
    @Override
    public String getHarvestTool(@Nonnull IBlockState state) {
        return this.harvestTool;
    }

    @Override
    public boolean isToolEffective(@Nonnull String type, @Nonnull IBlockState state) {
        return getHarvestTool(state).equals(type);
    }

    @Nonnull
    @Override
    public String getTranslationKey() {
        return this.internalName;
    }
}
