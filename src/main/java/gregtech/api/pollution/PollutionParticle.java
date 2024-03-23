package gregtech.api.pollution;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

class PollutionParticle extends Particle {

    PollutionParticle(World world, double posX, double posY, double posZ) {
        super(world, posX, posY, posZ);

        this.particleRed = 0.25F;
        this.particleGreen = 0.2F;
        this.particleBlue = 0.25F;

        this.motionX *= 0.1D;
        this.motionY *= -0.1D;
        this.motionZ *= 0.1F;

        this.motionX += world.rand.nextFloat() * -1.9D * world.rand.nextFloat() * 0.1D;
        this.motionY += world.rand.nextFloat() * -0.5D * world.rand.nextFloat() * 0.1D * 5.0D;
        this.motionZ += world.rand.nextFloat() * -1.9D * world.rand.nextFloat() * 0.1D;

        this.particleTextureIndexX = 0;
        this.particleTextureIndexY = 0;

        this.particleMaxAge = (int) (20 / (world.rand.nextFloat() * 0.8 + 0.2));

        this.particleScale *= 0.75F;
        this.canCollide = false;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            this.motionY -= 5.0E-4D;
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.posY == this.prevPosY) {
                this.motionX *= 1.1D;
                this.motionZ *= 1.1D;
            }
            this.motionX *= 0.96D;
            this.motionY *= 0.96D;
            this.motionZ *= 0.96D;
            if (this.onGround) {
                this.motionX *= 0.7D;
                this.motionZ *= 0.7D;
            }
        }
    }
}
