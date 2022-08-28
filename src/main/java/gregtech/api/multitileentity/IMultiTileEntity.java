package gregtech.api.multitileentity;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public interface IMultiTileEntity {

    void onAdjacentBlockChange(BlockPos currentPos);

    // Hooks into the Block Class. Implement them in order to overwrite the Default Behaviors.
    interface IMTE_OnNeighborChanged {
        void onNeighborChanged(@Nonnull IBlockAccess access, @Nonnull BlockPos pos);
    }
    interface IMTE_IsSideSolid {
        boolean isSideSolid(@Nonnull EnumFacing side);
    }
    interface IMTE_CanCreatureSpawn {
        boolean canCreatureSpawn(@Nonnull EntityLiving.SpawnPlacementType type);
    }
    interface IMTE_BlocksMovement {
        boolean blocksMovement();
    }
    interface IMTE_IsReplaceable {
        boolean isReplaceable();
    }
    interface IMTE_GetBlockHardness {
        float getBlockHardness();
    }
    interface IMTE_GetBoundingBox {
        @Nonnull AxisAlignedBB getBoundingBox();
    }
    interface IMTE_GetPackedLightmapCoords {
        int getPackedLightmapCoords();
    }
    interface IMTE_ShouldSideBeRendered {
        boolean shouldSideBeRendered(@Nonnull EnumFacing side);
    }
    interface IMTE_GetBlockFaceShape {
        @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull EnumFacing side);
    }
    interface IMTE_AddCollisionBoxesToList {
        void addCollisionBoxesToList(@Nonnull AxisAlignedBB aabb, @Nonnull List<AxisAlignedBB> list, @Nullable Entity entity);
    }
    interface IMTE_GetCollisionBoundingBox {
        @Nullable AxisAlignedBB getCollisionBoundingBox();
    }
    interface IMTE_RandomTick {
        void randomTick(@Nonnull Random random);
    }
    interface IMTE_UpdateTick {
        void updateTick(@Nonnull Random random);
    }
    interface IMTE_RandomDisplayTick {
        void randomDisplayTick(@Nonnull Random random);
    }
    interface IMTE_OnPlayerDestroy {
        void onPlayerDestroy();
    }
    interface IMTE_OnBlockAdded {
        void onBlockAdded();
    }
    interface IMTE_GetPlayerRelativeBlockHardness {
        float getPlayerRelativeBlockHardness(@Nonnull EntityPlayer player, float original);
    }
    interface IMTE_GetDrops {
        NonNullList<ItemStack> getDrops(int fortune, boolean isSilkTouch);
    }
    interface IMTE_DropXpOnBlockBreak {
        void dropXpOnBlockBreak(int xp);

        int getXpDropped(int fortune);
    }
    interface IMTE_CollisionRayTrace {
        @Nullable RayTraceResult collisionRayTrace(@Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end);
    }
    interface IMTE_OnBlockExploded {
        void onBlockExploded(@Nonnull Explosion explosion);
    }
    interface IMTE_CanPlaceBlockAt {
        default boolean canPlaceBlockOnSide(@Nonnull BlockPos targetPos, EnumFacing side) {
            return canPlaceBlockAt(targetPos);
        }

        boolean canPlaceBlockAt(@Nonnull BlockPos targetPos);
    }
    interface IMTE_OnBlockActivated {
        boolean onBlockActivated(@Nonnull EntityPlayer player, @Nonnull ItemStack heldStack, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ);
    }
    interface IMTE_OnEntityWalk {
        void onEntityWalk(@Nonnull Entity entity);
    }
    interface IMTE_OnBlockClicked {
        void onBlockClicked(@Nonnull EntityPlayer player);
    }
    interface IMTE_ModifyAcceleration {
        @Nonnull Vec3d modifyAcceleration(@Nonnull Entity entity, @Nonnull Vec3d movementVec);
    }
    interface IMTE_GetWeakPower {
        int getWeakPower(@Nonnull EnumFacing side);
    }
    interface IMTE_OnEntityCollision {
        void onEntityCollision(@Nonnull Entity entity);
    }
    interface IMTE_GetStrongPower {
        int getStrongPower(@Nonnull EnumFacing side);
    }
    interface IMTE_OnPlacedBy {
        void onBlockPlacedBy(@Nonnull EntityLivingBase placer, @Nonnull ItemStack blockStack);
    }
    interface IMTE_OnFallenUpon {
        void onFallenUpon(@Nonnull Entity entity, float fallDistance);
    }
    interface IMTE_GetStackFromBlock {
        @Nonnull ItemStack getStackFromBlock();
    }
    interface IMTE_OnBlockHarvested {
        void onBlockHarvested(@Nonnull EntityPlayer player);
    }
    interface IMTE_FillWithRain {
        void fillWithRain();
    }
    interface IMTE_GetComparatorInputOverride {
        int getComparatorInputOverride();
    }
    interface IMTE_GetSlipperiness {
        float getSlipperiness(@Nullable Entity entity);
    }
    interface IMTE_GetLightValue {
        int getLightValue();
    }
    interface IMTE_IsLadder {
        boolean isLadder(@Nonnull EntityLivingBase entity);
    }
    interface IMTE_IsNormalCube {
        boolean isNormalCube();
    }
    interface IMTE_DoesSideBlockRendering {
        boolean doesSideBlockRendering(@Nonnull EnumFacing side);
    }
    interface IMTE_IsBurning {
        boolean isBurning();
    }
    interface IMTE_IsAir {
        boolean isAir();
    }
    interface IMTE_RemovedByPlayer {
        boolean removedByPlayer(@Nonnull World world, @Nonnull EntityPlayer player, boolean willHarvest);
    }
    interface IMTE_GetFlammability {
        int getFlammability(@Nonnull EnumFacing side, boolean defaultValue);
    }
    interface IMTE_GetFireSpreadSpeed {
        int getFireSpreadSpeed(@Nonnull EnumFacing side, boolean defaultValue);
    }
    interface IMTE_IsFireSource {
        boolean isFireSource(@Nonnull EnumFacing side);
    }
    interface IMTE_Bed {
        boolean isBed(@Nullable Entity entity);

        @Nullable BlockPos getBedSpawnPosition(@Nullable EntityPlayer player);

        void setBedOccupied(@Nonnull EntityPlayer player, boolean occupied);

        @Nonnull EnumFacing getBedDirection();

        boolean isBedFoot();
    }
    interface IMTE_Leaves {
        void beginLeavesDecay();

        boolean canSustainLeaves();

        boolean isLeaves();
    }
    interface IMTE_CanBeReplacedByLeaves {
        boolean canBeReplacedByLeaves();
    }
    interface IMTE_IsWood {
        boolean isWood();
    }
    interface IMTE_IsReplaceableOregen {
        boolean isReplaceableOregen(@Nonnull Predicate<IBlockState> target);
    }
    interface IMTE_GetExplosionResistance {
        float getExplosionResistance(@Nullable Entity exploder, @Nonnull BlockPos pos);
    }
    interface IMTE_CanConnectRedstone {
        boolean canConnectRedstone(@Nullable EnumFacing side);
    }
    interface IMTE_CanPlaceTorchOnTop {
        boolean canPlaceTorchOnTop();
    }
    interface IMTE_GetPickBlock {
        @Nonnull ItemStack getPickBlock(RayTraceResult target);
    }
    interface IMTE_IsFoliage {
        boolean isFoliage();
    }
    interface IMTE_CanSustainPlant {
        boolean canSustainPlant(@Nonnull EnumFacing side, @Nonnull IPlantable plantable);
    }
    interface IMTE_OnPlantGrow {
        void onPlantGrow(@Nonnull BlockPos sourcePos);
    }
    interface IMTE_IsFertile {
        boolean isFertile();
    }
    interface IMTE_CanEntityDestroy {
        boolean canEntityDestroy(@Nonnull Entity entity);
    }
    interface IMTE_IsBeaconBase {
        boolean isBeaconBase();
    }
    interface IMTE_Rotateable {
        boolean rotateBlock(@Nonnull EnumFacing side);

        @Nonnull EnumFacing[] getValidRotations();
    }
    interface IMTE_GetEnchantPowerBonus {
        float getEnchantPowerBonus();
    }
    interface IMTE_RecolorBlock {
        boolean recolorBlock(@Nonnull EnumFacing side, @Nonnull EnumDyeColor color);
    }
    interface IMTE_ShouldCheckWeakPower {
        boolean shouldCheckWeakPower(@Nonnull EnumFacing side);
    }
    interface IMTE_GetWeakChanges {
        boolean getWeakChanges();
    }
    interface IMTE_GetSoundType {
        @Nonnull SoundType getSoundType(@Nullable Entity entity);
    }
    interface IMTE_GetBeaconColorMultiplier {
        @Nullable float[] getBeaconColorMultiplier(@Nonnull BlockPos beaconPos);
    }
    interface IMTE_GetFogColor {
        @Nonnull Vec3d getFogColor(@Nonnull Entity entity, @Nonnull Vec3d originalColor, float partialTicks);
    }
    interface IMTE_GetParticleTexture {
        @Nonnull Pair<TextureAtlasSprite, Integer> getParticleTexture();
    }
}
