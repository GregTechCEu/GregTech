package gregtech.common.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DynamiteEntity extends EntityThrowable {

    private int ticksUntilExplosion;
    private BlockPos blockPosCollidedAt;

    public DynamiteEntity(World worldIn) {
        super(worldIn);
    }

    public DynamiteEntity(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public DynamiteEntity(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    @Override
    protected void entityInit() {
        ticksUntilExplosion = 80 + world.rand.nextInt(60);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.sideHit == EnumFacing.UP) {
            inGround = true;
            blockPosCollidedAt = result.getBlockPos();
        } else {
            if (result.sideHit.getAxis() == EnumFacing.Axis.Z) {
                this.motionZ = 0;
            } else if (result.sideHit.getAxis() == EnumFacing.Axis.X) {
                this.motionX = 0;
            } else if (result.sideHit.getAxis() == EnumFacing.Axis.Y) {
                this.motionY = 0;
            }
        }
    }

    @Override
    public void onUpdate() {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        this.onEntityUpdate();

        ticksUntilExplosion--;

        if (world.rand.nextInt(3) == 2) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, -this.motionX * 0.05f,
                    this.inGround ? 0.05f : -this.motionY * 0.05f, -this.motionZ * 0.05f);
        }

        if (ticksUntilExplosion < 0 && !world.isRemote) {
            EntityLivingBase thrower = getThrower();
            world.createExplosion(thrower == null ? this : thrower, this.posX, this.posY, this.posZ, 1.5f, true);
            this.setDead();
            return;
        }

        if (this.inGround) {
            if (blockPosCollidedAt == null || this.world.getBlockState(blockPosCollidedAt).getBlock() != Blocks.AIR) {
                return;
            }

            this.inGround = false;
            this.motionX *= this.rand.nextFloat() * 0.2F;
            this.motionY *= this.rand.nextFloat() * 0.2F;
            this.motionZ *= this.rand.nextFloat() * 0.2F;
        }

        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d1 = new Vec3d(this.posX + this.motionX + (this.motionX > 0 ? 0.5f : -0.5f),
                this.posY + this.motionY + (this.motionY > 0 ? 0.2f : -0.2f),
                this.posZ + this.motionZ + (this.motionZ > 0 ? 0.2f : -0.2f));
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1);

        if (raytraceresult != null) {
            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK &&
                    this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL) {
                this.setPortal(raytraceresult.getBlockPos());
            } else if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact(raytraceresult);
            }
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(this.motionY, f) * (180D / Math.PI));

        while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
            this.prevRotationPitch -= 360.0F;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float f1 = 0.99F;
        float f2 = this.getGravityVelocity();

        if (this.isInWater()) {
            for (int j = 0; j < 4; ++j) {
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D,
                        this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY,
                        this.motionZ);
            }

            f1 = 0.8F;
        }

        this.motionX *= f1;
        this.motionY *= f1;
        this.motionZ *= f1;

        if (!this.hasNoGravity()) {
            this.motionY -= f2;
        }

        this.setPosition(this.posX, this.posY, this.posZ);
    }
}
