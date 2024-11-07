package gregtech.client.particle;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.RelativeDirection;
import gregtech.common.metatileentities.multi.MetaTileEntityNaturalDraftCooler;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public enum VanillaParticleEffects implements IMachineParticleEffect {

    TOP_SMOKE_SMALL(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;
        if (mte.getFrontFacing() == EnumFacing.UP || mte.hasCover(EnumFacing.UP)) return;

        BlockPos pos = mte.getPos();
        float x = pos.getX() + 0.8F - GTValues.RNG.nextFloat() * 0.6F;
        float y = pos.getY() + 0.9F + GTValues.RNG.nextFloat() * 0.2F;
        float z = pos.getZ() + 0.8F - GTValues.RNG.nextFloat() * 0.6F;
        mte.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
    }),

    PBF_SMOKE(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;

        BlockPos pos = mte.getPos();
        EnumFacing facing = mte.getFrontFacing().getOpposite();
        float xPos = facing.getXOffset() * 0.76F + pos.getX() + 0.5F;
        float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
        float zPos = facing.getZOffset() * 0.76F + pos.getZ() + 0.5F;

        float ySpd = facing.getYOffset() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        mte.getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, 0, ySpd, 0);
    }),

    RANDOM_LAVA_SMOKE(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;

        EnumFacing facing = mte.getFrontFacing();
        if (facing.getAxis() == EnumFacing.Axis.Y || mte.hasCover(facing)) return;
        BlockPos pos = mte.getPos();

        final double offX = pos.getX() + facing.getXOffset() + 0.5D;
        final double offY = pos.getY() + facing.getYOffset();
        final double offZ = pos.getZ() + facing.getZOffset() + 0.5D;
        final double offset = -0.48D;
        final double horizontal = GTValues.RNG.nextFloat() * 0.625D - 0.3125D;

        double x, z;
        double y = offY + GTValues.RNG.nextFloat() * 0.375D;

        if (facing == EnumFacing.WEST) {
            x = offX - offset;
            z = offZ + horizontal;
        } else if (facing == EnumFacing.EAST) {
            x = offX + offset;
            z = offZ + horizontal;
        } else if (facing == EnumFacing.NORTH) {
            x = offX + horizontal;
            z = offZ - offset;
        } else { // south
            x = offX + horizontal;
            z = offZ + offset;
        }

        mte.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
        mte.getWorld().spawnParticle(EnumParticleTypes.LAVA, x, y, z, 0, 0, 0);
    }),

    RANDOM_SPARKS(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;

        EnumFacing facing = mte.getFrontFacing();
        if (facing.getAxis() == EnumFacing.Axis.Y || mte.hasCover(facing)) return;

        if (GTValues.RNG.nextInt(3) == 0) {
            BlockPos pos = mte.getPos();

            final double offset = 0.02D;
            final double horizontal = 0.5D + GTValues.RNG.nextFloat() * 0.5D - 0.25D;

            double x, z, mX, mZ;
            double y = pos.getY() + GTValues.RNG.nextFloat() * 0.625D + 0.3125D;
            if (facing == EnumFacing.WEST) {
                x = pos.getX() - offset;
                mX = -0.05D;
                z = pos.getZ() + horizontal;
                mZ = 0.0D;
            } else if (facing == EnumFacing.EAST) {
                x = pos.getX() + offset;
                mX = 0.05D;
                z = pos.getZ() + horizontal;
                mZ = 0.0D;
            } else if (facing == EnumFacing.NORTH) {
                x = pos.getX() + horizontal;
                mX = 0.0D;
                z = pos.getZ() - offset;
                mZ = -0.05D;
            } else { // south
                x = pos.getX() + horizontal;
                mX = 0.0D;
                z = pos.getZ() + offset;
                mZ = 0.05D;
            }

            mte.getWorld().spawnParticle(EnumParticleTypes.LAVA, x, y, z, mX, 0, mZ);
        }
    }),

    COMBUSTION_SMOKE(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;
        if (mte.hasCover(EnumFacing.UP)) return;
        BlockPos pos = mte.getPos();

        float x = pos.getX() + 0.125F + GTValues.RNG.nextFloat() * 0.875F;
        float y = pos.getY() + 1.03125F;
        float z = pos.getZ() + 0.125F + GTValues.RNG.nextFloat() * 0.875F;

        mte.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
    }),

    NATURAL_DRAFT_EFFECTS(mte -> {
        if (mte.getWorld() == null || mte.getPos() == null) return;
        if (!(mte instanceof MetaTileEntityNaturalDraftCooler tower)) return;
        EnumFacing back = RelativeDirection.BACK.getRelativeFacing(tower.getFrontFacing(), tower.getUpwardsFacing(),
                tower.isFlipped());
        BlockPos center = mte.getPos().offset(back, 7);
        double xC = center.getX() + 0.5;
        double zC = center.getZ() + 0.5;
        double yLow = center.getY() + 4.5;
        double yHigh = center.getY() + 7;

        for (int i = 0; i < 100; i++) {
            float x = GTValues.RNG.nextFloat() * 8.8f - 4.4f;
            float z = GTValues.RNG.nextFloat() * 8.8f - 4.4f;
            // kill the spawn attempt if it exceeds the baffle range
            if ((Math.abs(x) > 3.4 && Math.abs(z) > 2.4) || (Math.abs(x) > 2.4 && Math.abs(z) > 3.4)) {
                continue;
            }
            // low particles - continuous water dripping
            mte.getWorld().spawnParticle(EnumParticleTypes.DRIP_WATER, xC + x, yLow + 0.5,
                    zC + z, 0, 0, 0);

            // high particles - condensation in the updraft
            // TODO custom particle that fits the application better
            double dx = GTValues.RNG.nextGaussian() * 0.05;
            double dy = GTValues.RNG.nextGaussian() * 0.1 + 0.3;
            double dz = GTValues.RNG.nextGaussian() * 0.05;
            mte.getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, xC + x, yHigh,
                    zC + z, dx, dy, dz);
            dx = GTValues.RNG.nextGaussian() * 0.05;
            dy = GTValues.RNG.nextGaussian() * 0.1 + 0.3;
            dz = GTValues.RNG.nextGaussian() * 0.05;
            mte.getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, xC + x, yHigh + 5,
                    zC + z, dx, dy, dz);
        }
    });

    // Wrap for client-sided stuff
    private final Consumer<MetaTileEntity> effectConsumer;

    VanillaParticleEffects(Consumer<MetaTileEntity> effectConsumer) {
        this.effectConsumer = effectConsumer;
    }

    @Override
    public void runEffect(@NotNull MetaTileEntity metaTileEntity) {
        effectConsumer.accept(metaTileEntity);
    }

    @SideOnly(Side.CLIENT)
    public static void defaultFrontEffect(@NotNull MetaTileEntity mte, EnumParticleTypes... particles) {
        defaultFrontEffect(mte, 0.0F, particles);
    }

    @SideOnly(Side.CLIENT)
    public static void defaultFrontEffect(@NotNull MetaTileEntity mte, float yOffset, EnumParticleTypes... particles) {
        if (particles == null || particles.length == 0) return;
        if (mte.getWorld() == null || mte.getPos() == null) return;

        BlockPos pos = mte.getPos();
        EnumFacing facing = mte.getFrontFacing();

        if (facing.getAxis() == EnumFacing.Axis.Y || mte.hasCover(facing)) return;

        float x = pos.getX() + 0.5F;
        float z = pos.getZ() + 0.5F;

        float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F + yOffset;
        float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F;

        if (facing.getAxis() == EnumFacing.Axis.X) {
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) x += 0.52F;
            else x -= 0.52F;
            z += horizontalOffset;
        } else if (facing.getAxis() == EnumFacing.Axis.Z) {
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) z += 0.52F;
            else z -= 0.52F;
            x += horizontalOffset;
        }

        for (EnumParticleTypes particle : particles) {
            mte.getWorld().spawnParticle(particle, x, y, z, 0, 0, 0);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void mufflerEffect(@NotNull MetaTileEntity mte, @NotNull EnumParticleTypes particle) {
        if (mte.getWorld() == null || mte.getPos() == null) return;

        BlockPos pos = mte.getPos();
        EnumFacing facing = mte.getFrontFacing();
        float xPos = facing.getXOffset() * 0.76F + pos.getX() + 0.25F;
        float yPos = facing.getYOffset() * 0.76F + pos.getY() + 0.25F;
        float zPos = facing.getZOffset() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = facing.getYOffset() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd;
        float zSpd;

        if (facing.getYOffset() == -1) {
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = facing.getXOffset() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
            zSpd = facing.getZOffset() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
        }

        xPos += GTValues.RNG.nextFloat() * 0.5F;
        yPos += GTValues.RNG.nextFloat() * 0.5F;
        zPos += GTValues.RNG.nextFloat() * 0.5F;

        mte.getWorld().spawnParticle(particle, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }
}
