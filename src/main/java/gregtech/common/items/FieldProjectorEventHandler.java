package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@EventBusSubscriber(modid = GTValues.MODID)
public class FieldProjectorEventHandler {

    private static final long EU_PER_PROJECTILE_DEFLECT = 1024L;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Entity hitEntity = event.getRayTraceResult().entityHit;
        if (hitEntity instanceof EntityLivingBase && canReflectProjectile((EntityLivingBase) hitEntity, true)) {
            Pair<Boolean, Entity> projectileData = isHostileProjectile(event.getEntity(), hitEntity);
            if (projectileData.getLeft()) {
                Entity shooterEntity = projectileData.getRight();
                Vec3d resultHitPosition;
                if (shooterEntity != null) {
                    double posY = shooterEntity.posY + shooterEntity.height / 3.0;
                    resultHitPosition = new Vec3d(shooterEntity.posX, posY, shooterEntity.posZ);
                } else {
                    Vec3d lookVector = hitEntity.getLookVec();
                    resultHitPosition = hitEntity.getPositionVector().add(lookVector.scale(5.0));
                }
                if (canReflectProjectile((EntityLivingBase) hitEntity, false)) {
                    redirectProjectile(event.getEntity(), resultHitPosition);
                    if (!hitEntity.world.isRemote) {
                        Vec3d arrowPos = event.getEntity().getPositionVector();
                        ((WorldServer) hitEntity.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, false,
                                arrowPos.x, arrowPos.y, arrowPos.z, 4, 0.0, 0.0, 0.0, 0.1);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean canReflectProjectile(EntityLivingBase entity, boolean simulate) {
        if (entity instanceof EntityPlayer) {
            IInventory inventory = ((EntityPlayer) entity).inventory;
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (tryDrainProjector(itemStack, simulate)) return true;
            }
        } else {
            for (EntityEquipmentSlot equipmentSlot : EntityEquipmentSlot.values()) {
                ItemStack itemStack = entity.getItemStackFromSlot(equipmentSlot);
                if (tryDrainProjector(itemStack, simulate)) return true;
            }
        }
        return false;
    }

    private static boolean tryDrainProjector(ItemStack itemStack, boolean simulate) {
        if (!MetaItems.ENERGY_FIELD_PROJECTOR.isItemEqual(itemStack)) {
            return false;
        }
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null && electricItem.discharge(EU_PER_PROJECTILE_DEFLECT,
                electricItem.getTier(), true, false, simulate) >= EU_PER_PROJECTILE_DEFLECT;
    }

    private static Pair<Boolean, Entity> isHostileProjectile(Entity entity, Entity owner) {
        if (entity instanceof EntityFireball) {
            EntityLivingBase shooter = ((EntityFireball) entity).shootingEntity;
            return Pair.of(!owner.isEntityEqual(shooter), shooter);

        } else if (entity instanceof EntityArrow) {
            Entity shooter = ((EntityArrow) entity).shootingEntity;
            return Pair.of(!owner.isEntityEqual(shooter), shooter);

        } else if (entity instanceof EntityPotion) {
            EntityLivingBase shooter = ((EntityPotion) entity).getThrower();
            ItemStack potionStack = ((EntityPotion) entity).getPotion();
            List<PotionEffect> effectList = PotionUtils.getEffectsFromStack(potionStack);
            boolean hasBadEffects = effectList.stream().anyMatch(it -> it.getPotion().isBadEffect());
            //potions without bad effects are not hostile, so do not touch them
            return Pair.of(!owner.isEntityEqual(shooter) && hasBadEffects, shooter);

        } else if (entity instanceof EntityThrowable) {
            EntityLivingBase shooter = ((EntityThrowable) entity).getThrower();
            return Pair.of(!owner.isEntityEqual(shooter), shooter);

        } else if (entity instanceof IThrowableEntity) {
            Entity shooter = ((IThrowableEntity) entity).getThrower();
            return Pair.of(!owner.isEntityEqual(shooter), shooter);

        } else if (entity instanceof IProjectile) {
            return Pair.of(true, null); //unknown projectiles are always hostile
        }
        return Pair.of(false, null);
    }

    private static void redirectProjectile(Entity entity, Vec3d targetPosition) {
        if (entity instanceof EntityFireball) {
            Vec3d directionVector = targetPosition.subtract(entity.getPositionVector()).normalize();
            //compute length of motion vector, and change direction keeping motion vector length
            float motionLength = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ) * 2.0f;
            entity.motionX = directionVector.x * motionLength;
            entity.motionY = directionVector.y * motionLength;
            entity.motionZ = directionVector.z * motionLength;
            //then, modify acceleration to point target entity
            ((EntityFireball) entity).accelerationX = directionVector.x * 0.1;
            ((EntityFireball) entity).accelerationY = directionVector.y * 0.1;
            ((EntityFireball) entity).accelerationZ = directionVector.z * 0.1;

        } else if (entity instanceof IProjectile) {
            Vec3d distanceVector = targetPosition.subtract(entity.getPositionVector());
            float motionLength = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ) * 2.0f;
            double verticalAccl = MathHelper.sqrt(distanceVector.x * distanceVector.x + distanceVector.z * distanceVector.z);
            ((IProjectile) entity).shoot(distanceVector.x, distanceVector.y + verticalAccl * 0.1, distanceVector.z, motionLength, 0.0f);
        }
        entity.velocityChanged = true;
    }

}
