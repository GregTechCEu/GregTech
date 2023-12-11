package gregtech.api.block.machines;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.BlockCustomParticle;
import gregtech.api.block.UnlistedIntegerProperty;
import gregtech.api.block.UnlistedStringProperty;
import gregtech.api.cover.Cover;
import gregtech.api.cover.IFacadeCover;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pipenet.IBlockAppearance;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.common.items.MetaItems;
import gregtech.integration.ctm.IFacadeWrapper;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

@SuppressWarnings("deprecation")
public class BlockMachine extends BlockCustomParticle implements ITileEntityProvider, IFacadeWrapper, IBlockAppearance {

    private static final List<IndexedCuboid6> EMPTY_COLLISION_BOX = Collections.emptyList();
    // used for rendering purposes of non-opaque machines like chests and tanks
    public static final PropertyBool OPAQUE = PropertyBool.create("opaque");

    // Vanilla MC's getHarvestTool() and getHarvestLevel() only pass the state, which is
    // not enough information to get the harvest tool and level from a MetaTileEntity on its own.
    // Using unlisted properties lets us get this information from getActualState(), which
    // provides enough information to get and read the MetaTileEntity data.
    private static final IUnlistedProperty<String> HARVEST_TOOL = new UnlistedStringProperty("harvest_tool");
    private static final IUnlistedProperty<Integer> HARVEST_LEVEL = new UnlistedIntegerProperty("harvest_level");

    public BlockMachine() {
        super(Material.IRON);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MACHINES);
        setSoundType(SoundType.METAL);
        setHardness(6.0f);
        setResistance(6.0f);
        setTranslationKey("unnamed");
        setDefaultState(getDefaultState().withProperty(OPAQUE, true));
    }

    @Nullable
    @Override
    public String getHarvestTool(@NotNull IBlockState state) {
        String value = ((IExtendedBlockState) state).getValue(HARVEST_TOOL);
        return value == null ? ToolClasses.WRENCH : value;
    }

    @Override
    public int getHarvestLevel(@NotNull IBlockState state) {
        Integer value = ((IExtendedBlockState) state).getValue(HARVEST_LEVEL);
        return value == null ? 1 : value;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return state.getValue(OPAQUE);
    }

    @NotNull
    @Override
    public IBlockState getActualState(@NotNull IBlockState state, @NotNull IBlockAccess worldIn,
                                      @NotNull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return state;

        return ((IExtendedBlockState) state)
                .withProperty(HARVEST_TOOL, metaTileEntity.getHarvestTool())
                .withProperty(HARVEST_LEVEL, metaTileEntity.getHarvestLevel());
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { OPAQUE },
                new IUnlistedProperty[] { HARVEST_TOOL, HARVEST_LEVEL });
    }

    @Override
    public float getPlayerRelativeBlockHardness(@NotNull IBlockState state, @NotNull EntityPlayer player,
                                                @NotNull World worldIn, @NotNull BlockPos pos) {
        // make sure our extended block state info is here for callers (since forge does not do it for us in this case)
        state = state.getBlock().getActualState(state, worldIn, pos);
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(OPAQUE, meta % 2 == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(OPAQUE) ? 0 : 1;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    @Override
    public float getBlockHardness(@NotNull IBlockState blockState, @NotNull World worldIn, @NotNull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockHardness();
    }

    @Override
    public float getExplosionResistance(@NotNull World world, @NotNull BlockPos pos, @Nullable Entity exploder,
                                        @NotNull Explosion explosion) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockResistance();
    }

    private static List<IndexedCuboid6> getCollisionBox(IBlockAccess blockAccess, BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        if (metaTileEntity == null) return EMPTY_COLLISION_BOX;
        List<IndexedCuboid6> collisionList = new ArrayList<>();
        metaTileEntity.addCollisionBoundingBox(collisionList);
        metaTileEntity.addCoverCollisionBoundingBox(collisionList);
        return collisionList;
    }

    @Override
    public boolean doesSideBlockRendering(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                          @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return state.isOpaqueCube() && getMetaTileEntity(world, pos) != null;
    }

    @NotNull
    @Override
    public ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, @NotNull World world,
                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        if (target instanceof CuboidRayTraceResult) {
            return metaTileEntity.getPickItem((CuboidRayTraceResult) target, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                      @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        for (Cuboid6 axisAlignedBB : getCollisionBox(worldIn, pos)) {
            AxisAlignedBB offsetBox = axisAlignedBB.aabb().offset(pos);
            if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@NotNull IBlockState blockState, @NotNull World worldIn,
                                            @NotNull BlockPos pos, @NotNull Vec3d start, @NotNull Vec3d end) {
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, getCollisionBox(worldIn, pos));
    }

    @Override
    public boolean rotateBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing axis) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null) return false;
        if (metaTileEntity.hasFrontFacing() && metaTileEntity.isValidFrontFacing(axis)) {
            metaTileEntity.setFrontFacing(axis);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(@NotNull World world, @NotNull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || !metaTileEntity.hasFrontFacing()) return null;
        return Arrays.stream(EnumFacing.VALUES)
                .filter(metaTileEntity::isValidFrontFacing)
                .toArray(EnumFacing[]::new);
    }

    @Override
    public boolean recolorBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing side,
                                @NotNull EnumDyeColor color) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || metaTileEntity.getPaintingColor() == color.colorValue)
            return false;
        metaTileEntity.setPaintingColor(color.colorValue);
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                @NotNull EntityLivingBase placer, ItemStack stack) {
        IGregTechTileEntity holder = (IGregTechTileEntity) worldIn.getTileEntity(pos);
        MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
        if (holder != null && sampleMetaTileEntity != null) {
            // TODO Fix this
            if (stack.hasDisplayName() && holder instanceof MetaTileEntityHolder) {
                ((MetaTileEntityHolder) holder).setCustomName(stack.getDisplayName());
            }
            MetaTileEntity metaTileEntity = holder.setMetaTileEntity(sampleMetaTileEntity);
            if (stack.hasTagCompound()) {
                // noinspection ConstantConditions
                metaTileEntity.initFromItemStackData(stack.getTagCompound());
            }
            if (metaTileEntity.isValidFrontFacing(EnumFacing.UP)) {
                metaTileEntity.setFrontFacing(EnumFacing.getDirectionFromEntityLiving(pos, placer));
            } else {
                metaTileEntity.setFrontFacing(placer.getHorizontalFacing().getOpposite());
            }
            if (metaTileEntity instanceof MultiblockControllerBase multi) {
                if (multi.allowsExtendedFacing()) {
                    EnumFacing frontFacing = multi.getFrontFacing();
                    if (frontFacing == EnumFacing.UP) {
                        multi.setUpwardsFacing(placer.getHorizontalFacing());
                    } else if (frontFacing == EnumFacing.DOWN) {
                        multi.setUpwardsFacing(placer.getHorizontalFacing().getOpposite());
                    }
                }
            }
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                if (metaTileEntity.getProxy() != null) {
                    metaTileEntity.getProxy().setOwner((EntityPlayer) placer);
                }
            }

            // Color machines on place if holding spray can in off-hand
            if (placer instanceof EntityPlayer) {
                ItemStack offhand = placer.getHeldItemOffhand();
                for (int i = 0; i < EnumDyeColor.values().length; i++) {
                    if (offhand.isItemEqual(MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((EntityPlayer) placer, worldIn,
                                pos, EnumHand.OFF_HAND, EnumFacing.UP, 0, 0, 0);
                        break;
                    }
                }
            }

            metaTileEntity.onPlacement();
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) {
            if (!metaTileEntity.keepsInventory()) {
                NonNullList<ItemStack> inventoryContents = NonNullList.create();
                metaTileEntity.clearMachineInventory(inventoryContents);
                for (ItemStack itemStack : inventoryContents) {
                    Block.spawnAsEntity(worldIn, pos, itemStack);
                }
            }
            metaTileEntity.dropAllCovers();
            metaTileEntity.onRemoval();

            tileEntities.set(metaTileEntity);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                         @NotNull IBlockState state, int fortune) {
        MetaTileEntity metaTileEntity = tileEntities.get() == null ? getMetaTileEntity(world, pos) : tileEntities.get();
        if (metaTileEntity == null) return;
        if (!metaTileEntity.shouldDropWhenDestroyed()) return;
        ItemStack itemStack = metaTileEntity.getStackForm();
        NBTTagCompound tagCompound = new NBTTagCompound();
        metaTileEntity.writeItemStackData(tagCompound);
        // only set item tag if it's not empty, so newly created items will stack with dismantled
        if (!tagCompound.isEmpty())
            itemStack.setTagCompound(tagCompound);
        // TODO Clean this up
        if (metaTileEntity.getHolder() instanceof MetaTileEntityHolder) {
            MetaTileEntityHolder holder = (MetaTileEntityHolder) metaTileEntity.getHolder();
            if (holder.hasCustomName()) {
                itemStack.setStackDisplayName(holder.getName());
            }
        }
        drops.add(itemStack);
        metaTileEntity.getDrops(drops, harvesters.get());
    }

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (metaTileEntity == null || rayTraceResult == null) {
            return false;
        }

        // try to click with a tool first
        Set<String> toolClasses = itemStack.getItem().getToolClasses(itemStack);
        if (!toolClasses.isEmpty() && metaTileEntity.onToolClick(playerIn, toolClasses, hand, rayTraceResult)) {
            ToolHelper.damageItem(itemStack, playerIn);
            ToolHelper.playToolSound(itemStack, playerIn);
            return true;
        }

        // then try to click with normal right hand
        return metaTileEntity.onRightClick(playerIn, hand, facing, rayTraceResult);
    }

    @Override
    public void onBlockClicked(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EntityPlayer playerIn) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return;
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult != null) {
            metaTileEntity.onCoverLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public boolean canConnectRedstone(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      @Nullable EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity != null && metaTileEntity.canConnectRedstone(side == null ? null : side.getOpposite());
    }

    @Override
    public boolean shouldCheckWeakPower(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                        @NotNull EnumFacing side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos,
                            @NotNull EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        return metaTileEntity == null ? 0 :
                metaTileEntity.getOutputRedstoneSignal(side == null ? null : side.getOpposite());
    }

    @Override
    public void neighborChanged(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos,
                                @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        TileEntity holder = worldIn.getTileEntity(pos);
        if (holder instanceof IGregTechTileEntity gregTechTile) {
            EnumFacing facing = GTUtility.getFacingToNeighbor(pos, fromPos);
            if (facing != null) gregTechTile.onNeighborChanged(facing);
            MetaTileEntity metaTileEntity = gregTechTile.getMetaTileEntity();
            if (metaTileEntity != null) {
                metaTileEntity.updateInputRedstoneSignals();
                metaTileEntity.onNeighborChanged();
            }
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess world, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        TileEntity holder = world.getTileEntity(pos);
        if (holder instanceof IGregTechTileEntity gregTechTile) {
            EnumFacing facing = GTUtility.getFacingToNeighbor(pos, neighbor);
            if (facing != null) gregTechTile.onNeighborChanged(facing);
        }
    }

    protected final ThreadLocal<MetaTileEntity> tileEntities = new ThreadLocal<>();

    @Override
    public void harvestBlock(@NotNull World worldIn, @NotNull EntityPlayer player, @NotNull BlockPos pos,
                             @NotNull IBlockState state, @Nullable TileEntity te, @NotNull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : ((IGregTechTileEntity) te).getMetaTileEntity());
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nullable World worldIn, int meta) {
        return new MetaTileEntityHolder();
    }

    @NotNull
    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return MetaTileEntityRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getValue(OPAQUE);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getValue(OPAQUE);
    }

    @NotNull
    @Override
    public BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state,
                                            @NotNull BlockPos pos, @NotNull EnumFacing face) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? BlockFaceShape.SOLID : metaTileEntity.getCoverFaceShape(face);
    }

    @Override
    public int getLightValue(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        // since it is called on neighbor blocks
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightValue();
    }

    @Override
    public int getLightOpacity(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        // since it is called on neighbor blocks
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightOpacity();
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        for (MetaTileEntity metaTileEntity : GregTechAPI.MTE_REGISTRY) {
            if (metaTileEntity.isInCreativeTab(tab)) {
                metaTileEntity.getSubItems(tab, items);
            }
        }
    }

    @NotNull
    @Override
    public IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, @Nullable EnumFacing side,
                                 @NotNull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @NotNull
    @Override
    public IBlockState getFacade(@NotNull IBlockAccess world, @NotNull BlockPos pos, EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity != null && side != null) {
            Cover cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFacadeCover facadeCover) {
                return facadeCover.getVisualState();
            }
        }
        return world.getBlockState(pos);
    }

    @NotNull
    @Override
    public IBlockState getVisualState(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return getFacade(world, pos, side);
    }

    @Override
    public boolean supportsVisualConnections() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return MetaTileEntityRenderer.getParticleTexture(world, blockPos);
    }

    @Override
    public boolean canEntityDestroy(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull Entity entity) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null) {
            return super.canEntityDestroy(state, world, pos, entity);
        }
        return !((entity instanceof EntityWither || entity instanceof EntityWitherSkull) &&
                metaTileEntity.getWitherProof());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@NotNull IBlockState stateIn, @NotNull World worldIn, @NotNull BlockPos pos,
                                  @NotNull Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) metaTileEntity.randomDisplayTick();
    }
}
