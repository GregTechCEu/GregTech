package gregtech.api.fluids.attribute;

import gregtech.api.GTValues;
import gregtech.api.util.EntityDamageUtil;
import gregtech.api.util.GTUtility;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(gregtechId("acid"),
            list -> list.add(I18n.format("gregtech.fluid.type_acid.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.acid_proof")),
            (w, b, f) -> {
                w.playSound(null, b, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                boolean gaseous = f.getFluid().isGaseous(f);
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    GTUtility.spawnParticles(w, facing, EnumParticleTypes.CRIT_MAGIC, b, 3 + GTValues.RNG.nextInt(2));
                }
                GTUtility.spawnParticles(w, gaseous ? EnumFacing.UP : EnumFacing.DOWN, EnumParticleTypes.CRIT_MAGIC,
                        b, 6 + GTValues.RNG.nextInt(4));
                float scalar = (float) Math.log(f.amount);
                List<EntityLivingBase> entities = w.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(b).grow(scalar * (gaseous ? 0.4 : 0.2)));
                for (EntityLivingBase entity : entities) {
                    EntityDamageUtil.applyChemicalDamage(entity, scalar * (gaseous ? 0.6f : 0.8f));
                }
                w.setBlockToAir(b);
            },
            (p, f) -> {
                p.world.playSound(null, p.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F,
                        1.0F);
                boolean gaseous = f.getFluid().isGaseous(f);
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    GTUtility.spawnParticles(p.world, facing, EnumParticleTypes.CRIT_MAGIC, p,
                            3 + GTValues.RNG.nextInt(2));
                }
                GTUtility.spawnParticles(p.world, gaseous ? EnumFacing.UP : EnumFacing.DOWN,
                        EnumParticleTypes.CRIT_MAGIC,
                        p, 6 + GTValues.RNG.nextInt(4));
                float scalar = (float) Math.log(f.amount);
                List<EntityLivingBase> entities = p.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(p.getPosition()).grow(scalar * (gaseous ? 0.4 : 0.2)));
                for (EntityLivingBase entity : entities) {
                    if (entity == p) continue;
                    EntityDamageUtil.applyChemicalDamage(entity, scalar * (gaseous ? 0.6f : 0.8f));
                }
                EntityDamageUtil.applyChemicalDamage(p, scalar * (gaseous ? 3f : 4f));
            });

    private FluidAttributes() {}
}
