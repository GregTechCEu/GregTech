package gregtech.api.util;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.particle.DigIconParticle;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleHandlerUtil {

    public static void addBlockRunningEffects(World worldObj, Entity entity, TextureAtlasSprite atlasSprite, int spriteColor) {
        if (atlasSprite == null) return;
        double posX = entity.posX + (GTValues.RNG.nextFloat() - 0.5) * entity.width;
        double posY = entity.getEntityBoundingBox().minY + 0.1;
        double posZ = entity.posZ + (GTValues.RNG.nextFloat() - 0.5) * entity.width;
        ParticleManager manager = Minecraft.getMinecraft().effectRenderer;

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        DigIconParticle digIconParticle = new DigIconParticle(worldObj, posX, posY, posZ, -entity.motionX * 4.0, 1.5, -entity.motionZ * 4.0, atlasSprite);
        digIconParticle.setRBGColorF(red, green, blue);
        manager.addEffect(digIconParticle);
    }

    public static void addBlockLandingEffects(World worldObj, Vector3 entityPos, TextureAtlasSprite atlasSprite, int spriteColor, ParticleManager manager, int numParticles) {
        if (atlasSprite == null) return;
        Vector3 start = entityPos.copy();
        Vector3 end = start.copy().add(Vector3.down.copy().multiply(4));
        RayTraceResult traceResult = worldObj.rayTraceBlocks(start.vec3(), end.vec3(), true, false, true);
        double speed = 0.15;

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        if (traceResult != null && traceResult.typeOfHit == Type.BLOCK && numParticles != 0) {
            for (int i = 0; i < numParticles; i++) {
                double mX = GTValues.RNG.nextGaussian() * speed;
                double mY = GTValues.RNG.nextGaussian() * speed;
                double mZ = GTValues.RNG.nextGaussian() * speed;
                DigIconParticle digIconParticle = DigIconParticle.newLandingParticle(worldObj, entityPos.x, entityPos.y, entityPos.z, mX, mY, mZ, atlasSprite);
                digIconParticle.setRBGColorF(red, green, blue);
                manager.addEffect(digIconParticle);
            }
        }
    }

    public static void addBlockDestroyEffects(World worldObj, CuboidRayTraceResult result, TextureAtlasSprite atlasSprite, int spriteColor, ParticleManager manager) {
        addBlockDestroyEffects(worldObj, result.cuboid6, atlasSprite, spriteColor, manager);
    }

    public static void addBlockDestroyEffects(IBlockState blockState, World worldObj, BlockPos blockPos, TextureAtlasSprite atlasSprite, int spriteColor, ParticleManager manager) {
        Cuboid6 cuboid6 = new Cuboid6(blockState.getBoundingBox(worldObj, blockPos)).add(blockPos);
        addBlockDestroyEffects(worldObj, cuboid6, atlasSprite, spriteColor, manager);
    }

    public static void addHitEffects(World worldObj, CuboidRayTraceResult result, TextureAtlasSprite atlasSprite, int spriteColor, ParticleManager manager) {
        addBlockHitEffects(worldObj, result.cuboid6, result.sideHit, atlasSprite, spriteColor, manager);
    }

    public static void addHitEffects(IBlockState blockState, World worldObj, RayTraceResult target, TextureAtlasSprite atlasSprite, int spriteColor, ParticleManager manager) {
        Cuboid6 cuboid6 = getBoundingBox(blockState, worldObj, target);
        addBlockHitEffects(worldObj, cuboid6, target.sideHit, atlasSprite, spriteColor, manager);
    }

    private static Cuboid6 getBoundingBox(IBlockState blockState, World world, RayTraceResult target) {
        BlockPos blockPos = target.getBlockPos();
        if (target instanceof CuboidRayTraceResult) {
            return ((CuboidRayTraceResult) target).cuboid6.copy().add(blockPos);
        }
        return new Cuboid6(blockState.getBoundingBox(world, blockPos)).add(blockPos);
    }

    //Straight copied from CustomParticleHandler with color parameter added

    public static void addBlockHitEffects(World world, Cuboid6 bounds, EnumFacing side, TextureAtlasSprite icon, int spriteColor, ParticleManager particleManager) {
        if (icon == null) return;
        float border = 0.1F;
        Vector3 diff = bounds.max.copy().subtract(bounds.min).add(-2 * border);
        diff.x *= world.rand.nextDouble();
        diff.y *= world.rand.nextDouble();
        diff.z *= world.rand.nextDouble();
        Vector3 pos = diff.add(bounds.min).add(border);

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        if (side == EnumFacing.DOWN) {
            diff.y = bounds.min.y - border;
        }
        if (side == EnumFacing.UP) {
            diff.y = bounds.max.y + border;
        }
        if (side == EnumFacing.NORTH) {
            diff.z = bounds.min.z - border;
        }
        if (side == EnumFacing.SOUTH) {
            diff.z = bounds.max.z + border;
        }
        if (side == EnumFacing.WEST) {
            diff.x = bounds.min.x - border;
        }
        if (side == EnumFacing.EAST) {
            diff.x = bounds.max.x + border;
        }

        DigIconParticle digIconParticle = new DigIconParticle(world, pos.x, pos.y, pos.z, 0, 0, 0, icon);
        digIconParticle.multiplyVelocity(0.2F);
        digIconParticle.multipleParticleScaleBy(0.6F);
        digIconParticle.setRBGColorF(red, green, blue);
        particleManager.addEffect(digIconParticle);
    }

    public static void addBlockDestroyEffects(World world, Cuboid6 bounds, TextureAtlasSprite icon, int spriteColor, ParticleManager particleManager) {
        if (icon == null) return;
        Vector3 diff = bounds.max.copy().subtract(bounds.min);
        Vector3 center = bounds.min.copy().add(bounds.max).multiply(0.5);
        Vector3 density = diff.copy().multiply(4).ceil();

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        for (int i = 0; i < density.x; ++i) {
            for (int j = 0; j < density.y; ++j) {
                for (int k = 0; k < density.z; ++k) {
                    double x = bounds.min.x + (i + 0.5) * diff.x / density.x;
                    double y = bounds.min.y + (j + 0.5) * diff.y / density.y;
                    double z = bounds.min.z + (k + 0.5) * diff.z / density.z;
                    DigIconParticle digIconParticle = new DigIconParticle(world, x, y, z, x - center.x, y - center.y, z - center.z, icon);
                    digIconParticle.setRBGColorF(red, green, blue);
                    particleManager.addEffect(digIconParticle);
                }
            }
        }
    }
}
