package gregtech.common.entities;

import gregtech.api.util.BlockUtility;
import gregtech.api.util.GregFakePlayer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

public class PowderbarrelEntity extends EntityTNTPrimed {

    protected float fakeExplosionStrength = 3.5F;
    protected float range = 2;

    public PowderbarrelEntity(World world, double x, double y, double z, EntityLivingBase igniter) {
        super(world, x, y, z, igniter);
    }

    public PowderbarrelEntity(World world) {
        super(world);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
        }

        setFuse(this.getFuse() - 1);
        if (this.getFuse() <= 0) {
            this.setDead();
            if (!this.world.isRemote && !this.inWater) {
                this.explode();
            }
        } else {
            this.handleWaterMovement();
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D,
                    0.0D);
        }
    }

    protected void explode() {
        this.world.createExplosion(this, this.posX, this.posY + (double) (this.height / 16.0F), this.posZ,
                fakeExplosionStrength, false);
        EntityPlayer player = GregFakePlayer.get((WorldServer) world);

        for (BlockPos pos : BlockPos.getAllInBox(this.getPosition().add(-range, -range, -range),
                this.getPosition().add(range, range, range))) {
            IBlockState state = world.getBlockState(pos);
            float hardness = state.getBlockHardness(world, pos);
            float resistance = state.getBlock().getExplosionResistance(player);

            // todo need to do more checks here, most importantly for fluid blocks
            if (hardness >= 0.0f && resistance <= 100 && world.isBlockModifiable(player, pos)) {
                List<ItemStack> drops = attemptBreakBlockAndObtainDrops(pos, state, player);

                for (ItemStack stack : drops) {
                    EntityItem entity = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                    entity.setDefaultPickupDelay();
                    world.spawnEntity(entity);
                }
            }
        }
    }

    private List<ItemStack> attemptBreakBlockAndObtainDrops(BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getBlock().removedByPlayer(state, world, pos, player, true)) {
            world.playEvent(null, 2001, pos, Block.getStateId(state));
            state.getBlock().onPlayerDestroy(world, pos, state);

            BlockUtility.startCaptureDrops();
            state.getBlock().harvestBlock(world, player, pos, state, world.getTileEntity(pos), ItemStack.EMPTY);
            return BlockUtility.stopCaptureDrops();
        }
        return Collections.emptyList();
    }
}
