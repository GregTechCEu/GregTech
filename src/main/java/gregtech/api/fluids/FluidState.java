package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.fluids.attribute.AttributedFluid;

import gregtech.api.util.EntityDamageUtil;

import gregtech.api.util.GTUtility;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum FluidState implements ContainmentFailureHandler {

    LIQUID("gregtech.fluid.state_liquid"),
    GAS("gregtech.fluid.state_gas"),
    PLASMA("gregtech.fluid.state_plasma");

    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    public @NotNull String getTranslationKey() {
        return this.translationKey;
    }

    public static FluidState inferState(FluidStack stack) {
        if (stack.getFluid() instanceof AttributedFluid fluid) return fluid.getState();
        else return stack.getFluid().isGaseous(stack) ? GAS : LIQUID;
    }

    @Override
    public void handleFailure(World world, BlockPos failingBlock, FluidStack failingStack) {
        world.playSound(null, failingBlock, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        switch (this) {
            default -> {
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    int particles = GTValues.RNG.nextInt(5);
                    if (particles != 0) {
                        GTUtility.spawnParticles(world, facing, EnumParticleTypes.DRIP_WATER, failingBlock, particles);
                    }
                }
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingBlock).grow(scalar * 0.5));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar, 20);
                }
                world.setBlockToAir(failingBlock);
            }
            case GAS -> {
                GTUtility.spawnParticles(world, EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, failingBlock, 9 + GTValues.RNG.nextInt(3));
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingBlock).grow(scalar));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar * 0.75f, 15);
                }
                world.setBlockToAir(failingBlock);
            }
            case PLASMA -> {
                GTUtility.spawnParticles(world, EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, failingBlock, 3 + GTValues.RNG.nextInt(3));
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingBlock).grow(scalar * 1.5));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar, 30);
                }
                world.setBlockToAir(failingBlock);
                world.createExplosion(null, failingBlock.getX() + 0.5, failingBlock.getY() + 0.5, failingBlock.getZ() + 0.5,
                        1.0f + GTValues.RNG.nextFloat(), true);
            }
        }
    }

    @Override
    public void handleFailure(EntityPlayer failingPlayer, FluidStack failingStack) {
        World world = failingPlayer.world;
        world.playSound(null, failingPlayer.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        switch (this) {
            default -> {
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    int particles = GTValues.RNG.nextInt(5);
                    if (particles != 0) {
                        GTUtility.spawnParticles(world, facing, EnumParticleTypes.DRIP_WATER, failingPlayer, particles);
                    }
                }
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingPlayer.getPosition()).grow(scalar * 0.5));
                for (EntityLivingBase entity : entities) {
                    if (entity == failingPlayer) continue;
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar, 20);
                }
                EntityDamageUtil.applyTemperatureDamage(failingPlayer, failingStack.getFluid().getTemperature(failingStack),
                        scalar * 3, 60);
            }
            case GAS -> {
                GTUtility.spawnParticles(world, EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, failingPlayer, 9 + GTValues.RNG.nextInt(3));
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingPlayer.getPosition()).grow(scalar));
                for (EntityLivingBase entity : entities) {
                    if (entity == failingPlayer) continue;
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar * 0.75f, 15);
                }
                EntityDamageUtil.applyTemperatureDamage(failingPlayer, failingStack.getFluid().getTemperature(failingStack),
                        scalar * 2.25f, 45);
            }
            case PLASMA -> {
                GTUtility.spawnParticles(world, EnumFacing.UP, EnumParticleTypes.SMOKE_LARGE, failingPlayer, 3 + GTValues.RNG.nextInt(3));
                float scalar = (float) Math.log(failingStack.amount);
                List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(failingPlayer.getPosition()).grow(scalar * 1.5));
                for (EntityLivingBase entity : entities) {
                    if (entity == failingPlayer) continue;
                    EntityDamageUtil.applyTemperatureDamage(entity, failingStack.getFluid().getTemperature(failingStack),
                            scalar, 30);
                }
                EntityDamageUtil.applyTemperatureDamage(failingPlayer, failingStack.getFluid().getTemperature(failingStack),
                        scalar * 3, 90);
                Vec3d vec = failingPlayer.getPositionEyes(1);
                world.createExplosion(null, vec.x, vec.y, vec.z,
                        1.0f + GTValues.RNG.nextFloat(), true);
            }
        }
    }
}
