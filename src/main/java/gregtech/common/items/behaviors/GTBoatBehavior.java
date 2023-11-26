package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.entities.GTBoatEntity;
import gregtech.common.entities.GTBoatEntity.GTBoatType;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GTBoatBehavior implements IItemBehaviour {

    private final GTBoatType type;

    public GTBoatBehavior(GTBoatType type) {
        this.type = type;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        // almost exact copy of ItemBoat#onItemRightClick
        ItemStack stack = player.getHeldItem(hand);
        float realPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch);
        float realYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw);
        double realX = player.prevPosX + (player.posX - player.prevPosX);
        double realY = player.prevPosY + (player.posY - player.prevPosY) + (double) player.getEyeHeight();
        double realZ = player.prevPosZ + (player.posZ - player.prevPosZ);
        Vec3d realPos = new Vec3d(realX, realY, realZ);

        final float R2D = (float) (Math.PI / 180.0);

        float yawCos = MathHelper.cos(-realYaw * R2D - (float) Math.PI);
        float yawSin = MathHelper.sin(-realYaw * R2D - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-realPitch * R2D);
        float pitchSin = MathHelper.sin(-realPitch * R2D);
        Vec3d lookPos = realPos.add(
                (double) (yawSin * pitchCos) * 5,
                (double) pitchSin * 5,
                (double) (yawCos * pitchCos) * 5);
        RayTraceResult ray = world.rayTraceBlocks(realPos, lookPos, true);

        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        Vec3d relativeLook = player.getLook(1);

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox()
                .expand(relativeLook.x * 5, relativeLook.y * 5, relativeLook.z * 5)
                .grow(1))) {
            if (entity.canBeCollidedWith() &&
                    entity.getEntityBoundingBox()
                            .grow(entity.getCollisionBorderSize())
                            .contains(realPos)) {
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }
        }

        Block block = world.getBlockState(ray.getBlockPos()).getBlock();
        boolean rayHitWater = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        GTBoatEntity boat = new GTBoatEntity(world, ray.hitVec.x, rayHitWater ? ray.hitVec.y - 0.12 : ray.hitVec.y,
                ray.hitVec.z);
        boat.setGTBoatType(this.type);
        boat.rotationYaw = player.rotationYaw;

        if (!world.getCollisionBoxes(boat, boat.getEntityBoundingBox().grow(-0.1)).isEmpty()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            world.spawnEntity(boat);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
