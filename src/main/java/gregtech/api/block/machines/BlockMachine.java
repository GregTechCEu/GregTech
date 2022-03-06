package gregtech.api.block.machines;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.BlockCustomParticle;
import gregtech.api.block.UnlistedIntegerProperty;
import gregtech.api.block.UnlistedStringProperty;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover.IFacadeCover;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.IBlockAppearance;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.tools.DamageValues;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

@SuppressWarnings("deprecation")
public class BlockMachine extends BlockCustomParticle implements ITileEntityProvider, IFacadeWrapper, IBlockAppearance {

    private static final List<IndexedCuboid6> EMPTY_COLLISION_BOX = Collections.emptyList();
    private static final IUnlistedProperty<String> HARVEST_TOOL = new UnlistedStringProperty("harvest_tool");
    private static final IUnlistedProperty<Integer> HARVEST_LEVEL = new UnlistedIntegerProperty("harvest_level");
    //used for rendering purposes of non-opaque machines like chests and tanks
    public static final PropertyBool OPAQUE = PropertyBool.create("opaque");

    public BlockMachine() {
        super(Material.IRON);
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setSoundType(SoundType.METAL);
        setHardness(6.0f);
        setResistance(6.0f);
        setTranslationKey("unnamed");
        setDefaultState(getDefaultState().withProperty(OPAQUE, true));
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        if (ConfigHolder.machines.requireWrenchForMachines) {
            return player.getHeldItemMainhand().hasCapability(GregtechCapabilities.CAPABILITY_WRENCH, null);
        }
        return super.canHarvestBlock(world, pos, player);
    }

    @Nullable
    @Override
    public String getHarvestTool(@Nonnull IBlockState state) {
        String value = ((IExtendedBlockState) state).getValue(HARVEST_TOOL);
        return value == null ? "wrench" : value; //safety check for mods who don't handle state properly
    }

    @Override
    public int getHarvestLevel(@Nonnull IBlockState state) {
        Integer value = ((IExtendedBlockState) state).getValue(HARVEST_LEVEL);
        return value == null ? 0 : value; //safety check for mods who don't handle state properly
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return state.getValue(OPAQUE);
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null)
            return state;

        return ((IExtendedBlockState) state)
                .withProperty(HARVEST_TOOL, metaTileEntity.getHarvestTool() == null ? "wrench" : metaTileEntity.getHarvestTool())
                .withProperty(HARVEST_LEVEL, metaTileEntity.getHarvestLevel());
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{OPAQUE}, new IUnlistedProperty[]{HARVEST_TOOL, HARVEST_LEVEL});
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(OPAQUE, meta % 2 == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(OPAQUE) ? 0 : 1;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    @Override
    public float getBlockHardness(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockHardness();
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockResistance();
    }

    private List<IndexedCuboid6> getCollisionBox(IBlockAccess blockAccess, BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        if (metaTileEntity == null)
            return EMPTY_COLLISION_BOX;
        ArrayList<IndexedCuboid6> collisionList = new ArrayList<>();
        metaTileEntity.addCollisionBoundingBox(collisionList);
        metaTileEntity.addCoverCollisionBoundingBox(collisionList);
        return collisionList;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return state.isOpaqueCube() && getMetaTileEntity(world, pos) != null;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        if (target instanceof CuboidRayTraceResult) {
            return metaTileEntity.getPickItem((CuboidRayTraceResult) target, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        for (Cuboid6 axisAlignedBB : getCollisionBox(worldIn, pos)) {
            AxisAlignedBB offsetBox = axisAlignedBB.aabb().offset(pos);
            if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, getCollisionBox(worldIn, pos));
    }

    @Override
    public boolean rotateBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
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
    public EnumFacing[] getValidRotations(@Nonnull World world, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || !metaTileEntity.hasFrontFacing()) return null;
        return Arrays.stream(EnumFacing.VALUES)
                .filter(metaTileEntity::isValidFrontFacing)
                .toArray(EnumFacing[]::new);
    }

    @Override
    public boolean recolorBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EnumDyeColor color) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null ||
                metaTileEntity.getPaintingColor() == color.colorValue)
            return false;
        metaTileEntity.setPaintingColor(color.colorValue);
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, ItemStack stack) {
        IGregTechTileEntity holder = (IGregTechTileEntity) worldIn.getTileEntity(pos);
        MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
        if (holder != null && sampleMetaTileEntity != null) {
            // TODO Fix this
            if (stack.hasDisplayName() && holder instanceof MetaTileEntityHolder) {
                ((MetaTileEntityHolder) holder).setCustomName(stack.getDisplayName());
            }
            MetaTileEntity metaTileEntity = holder.setMetaTileEntity(sampleMetaTileEntity);
            if (stack.hasTagCompound()) {
                //noinspection ConstantConditions
                metaTileEntity.initFromItemStackData(stack.getTagCompound());
            }
            if (metaTileEntity.isValidFrontFacing(EnumFacing.UP)) {
                metaTileEntity.setFrontFacing(EnumFacing.getDirectionFromEntityLiving(pos, placer));
            } else {
                metaTileEntity.setFrontFacing(placer.getHorizontalFacing().getOpposite());
            }
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                if (metaTileEntity.getProxy() != null) {
                    metaTileEntity.getProxy().setOwner((EntityPlayer) placer);
                }
            }

            // Color machines on place if holding spray can in off-hand
            if(placer instanceof EntityPlayer) {
                ItemStack offhand = placer.getHeldItemOffhand();
                for(int i  = 0; i < EnumDyeColor.values().length; i++) {
                    if(offhand.isItemEqual(MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((EntityPlayer) placer, worldIn, pos, EnumHand.OFF_HAND, EnumFacing.UP, 0, 0 , 0);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
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
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
        MetaTileEntity metaTileEntity = tileEntities.get() == null ? getMetaTileEntity(world, pos) : tileEntities.get();
        if (metaTileEntity == null) return;
        if (!metaTileEntity.shouldDropWhenDestroyed())
            return;
        ItemStack itemStack = metaTileEntity.getStackForm();
        NBTTagCompound tagCompound = new NBTTagCompound();
        metaTileEntity.writeItemStackData(tagCompound);
        //only set item tag if it's not empty, so newly created items will stack with dismantled
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
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (metaTileEntity == null || rayTraceResult == null) {
            return false;
        }
        if (itemStack.getItem().getToolClasses(itemStack).contains("screwdriver")) {
            metaTileEntity.onCoverScrewdriverClick(playerIn, hand, rayTraceResult);
            ToolHelper.damageItem(itemStack, playerIn, DamageValues.DAMAGE_FOR_SCREWDRIVER);
            if (itemStack.getItem() instanceof IGTTool) {
                ((IGTTool) itemStack.getItem()).playSound(playerIn);
            }
        } else if (itemStack.getItem().getToolClasses(itemStack).contains("wrench")) {
            metaTileEntity.onWrenchClick(playerIn, hand, ICoverable.determineGridSideHit(rayTraceResult), rayTraceResult);
            ToolHelper.damageItem(itemStack, playerIn, DamageValues.DAMAGE_FOR_WRENCH);
            if (itemStack.getItem() instanceof IGTTool) {
                ((IGTTool) itemStack.getItem()).playSound(playerIn);
            }
        }
        return metaTileEntity.onCoverRightClick(playerIn, hand, rayTraceResult);
    }

    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return;
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult != null) {
            metaTileEntity.onCoverLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity != null && metaTileEntity.canConnectRedstone(side == null ? null : side.getOpposite());
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getOutputRedstoneSignal(side == null ? null : side.getOpposite());
    }

    @Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) {
            metaTileEntity.updateInputRedstoneSignals();
            metaTileEntity.onNeighborChanged();
        }
    }

    @Override
    public int getComparatorInputOverride(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getComparatorValue();
    }

    protected final ThreadLocal<MetaTileEntity> tileEntities = new ThreadLocal<>();

    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : ((IGregTechTileEntity) te).getMetaTileEntity());
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Override
    public boolean hasComparatorInputOverride(@Nonnull IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nullable World worldIn, int meta) {
        return new MetaTileEntityHolder();
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return MetaTileEntityRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
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

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? BlockFaceShape.SOLID : metaTileEntity.getCoverFaceShape(face);
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightValue();
    }

    @Override
    public int getLightOpacity(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightOpacity();
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        for (MetaTileEntity metaTileEntity : GregTechAPI.MTE_REGISTRY) {
            metaTileEntity.getSubItems(tab, items);
        }
    }

    @Nonnull
    @Override
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side, @Nonnull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @Nonnull
    @Override
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity != null && side != null) {
            CoverBehavior coverBehavior = metaTileEntity.getCoverAtSide(side);
            if (coverBehavior instanceof IFacadeCover) {
                return ((IFacadeCover) coverBehavior).getVisualState();
            }
        }
        return world.getBlockState(pos);
    }

    @Nonnull
    @Override
    public IBlockState getVisualState(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return getFacade(world, pos, side);
    }

    @Override
    public boolean supportsVisualConnections() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return MetaTileEntityRenderer.INSTANCE.getParticleTexture(world, blockPos);
    }

    @Override
    public boolean canEntityDestroy(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if(metaTileEntity == null) {
            return super.canEntityDestroy(state, world, pos, entity);
        }
        return !((entity instanceof EntityWither || entity instanceof EntityWitherSkull) && metaTileEntity.getWitherProof());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) metaTileEntity.randomDisplayTick();
    }
}
