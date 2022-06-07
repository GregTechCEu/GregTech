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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

public class PowderbarrelEntity extends EntityTNTPrimed {

    protected float fakeExplosionStrength = 3.5F;
    protected float range = 2;

    public PowderbarrelEntity(World worldIn, double x, double y, double z, EntityLivingBase igniter) {
        super(worldIn, x, y, z, igniter);
    }

    public PowderbarrelEntity(World worldIn) {
        super(worldIn);
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
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
        }

    }

    protected void explode() {
        this.world.createExplosion(this, this.posX, this.posY + (double) (this.height / 16.0F), this.posZ, fakeExplosionStrength, false);

        EntityPlayer entityPlayer = GregFakePlayer.get((WorldServer) world);

        for (BlockPos blockPos : BlockPos.getAllInBox(this.getPosition().add(-range, -range, -range), this.getPosition().add(range, range, range))) {
            IBlockState blockState = world.getBlockState(blockPos);
            float hardness = blockState.getBlockHardness(world, blockPos);
            float resistance = blockState.getBlock().getExplosionResistance(entityPlayer);

            if (hardness >= 0.0f && resistance <= 100 && world.isBlockModifiable(entityPlayer, blockPos)) {
                List<ItemStack> drops = attemptBreakBlockAndObtainDrops(blockPos, blockState, entityPlayer);

                for (ItemStack itemStack : drops) {
                    EntityItem entityitem = new EntityItem(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                    entityitem.setDefaultPickupDelay();
                    world.spawnEntity(entityitem);
                }
            }
        }
    }

    public List<ItemStack> attemptBreakBlockAndObtainDrops(BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer) {
        TileEntity tileEntity = world.getTileEntity(blockPos);
        boolean result = blockState.getBlock().removedByPlayer(blockState, world, blockPos, entityPlayer, true);
        if (result) {
            world.playEvent(null, 2001, blockPos, Block.getStateId(blockState));
            blockState.getBlock().onPlayerDestroy(world, blockPos, blockState);

            BlockUtility.startCaptureDrops();
            blockState.getBlock().harvestBlock(world, entityPlayer, blockPos, blockState, tileEntity, ItemStack.EMPTY);
            return BlockUtility.stopCaptureDrops();
        }
        return Collections.emptyList();
    }
}
