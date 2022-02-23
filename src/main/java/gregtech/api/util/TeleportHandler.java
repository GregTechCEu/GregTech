package gregtech.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;

public class TeleportHandler {

    public static World getWorldByDimensionID(int id) {
        World world = DimensionManager.getWorld(id);
        if (world == null) {
            world = DimensionManager.getWorld(0).getMinecraftServer().getWorld(id);
        }
        return world;
    }

    protected static void unleashEntity(Entity entity) {
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).clearLeashed(true, false);
        }
        for (EntityLiving entity2 : entitiesWithinLeashRange(entity)) {
            if (entity2.getLeashed() && entity2.getLeashHolder() == entity) {
                entity2.clearLeashed(true, false);
            }
        }
    }

    protected static List<EntityLiving> entitiesWithinLeashRange(Entity entity) {
        AxisAlignedBB box = new AxisAlignedBB(
                entity.posX - 7.0D, entity.posY - 7.0D, entity.posZ - 7.0D,
                entity.posX + 7.0D, entity.posY + 7.0D, entity.posZ + 7.0D);
        return entity.world.getEntitiesWithinAABB(EntityLiving.class, box);
    }

    public static Entity teleportEntityAndRiders(Entity entity, int dimension, double x, double y, double z) {
        boolean canProceed = true;
        // Check if player is riding an entity.
        List<Entity> riders = entity.getPassengers();
        for (int i = 0; i < riders.size(); i++) {
            Entity rider = riders.get(i);
            rider.dismountRidingEntity();
            rider = teleportEntityAndRiders(rider, dimension, x, y, z);
            riders.set(i, rider);
        }

        unleashEntity(entity);
        entity = teleportEntity(entity, x, y, z, dimension);

        if (entity != null && !entity.isDead) {
            for (Entity rider : riders) {
                if (rider != null && !rider.isDead) {
                    rider.startRiding(entity, true);
                }
            }
        }
        return entity;
    }

    public static Entity teleportEntity(Entity entity, double x, double y, double z, int dimension) {
        Entity newEntity = null;

        if (entity.dimension == dimension) {
            newEntity = teleportWithinDimension(entity, x, y, z);
        } else {
            newEntity = teleportToOtherDimension(entity, x, y, z, dimension);
        }
        return newEntity;
    }

    public static Entity teleportWithinDimension(Entity entity, double x, double y, double z) {
        if (entity instanceof EntityPlayerMP) {
            return teleportPlayerWithinDimension((EntityPlayerMP) entity, x, y, z);
        } else {
            return teleportEntityToWorld(entity, x, y, z, DimensionManager.getWorld(0).getMinecraftServer().getWorld(entity.dimension));
        }
    }

    public static Entity teleportPlayerWithinDimension(EntityPlayerMP entity, double x, double y, double z) {
        setEntityLocation(entity, x, y, z);
        entity.world.updateEntityWithOptionalForce(entity, false);
        entity.velocityChanged = true; // Have to mark entity velocity changed.

        return entity;
    }

    public static Entity teleportToOtherDimension(Entity entity, double x, double y, double z, int dimension) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)entity;
            transferPlayerToDimension(player, dimension, x, y, z);
            return player;
        } else {
            return teleportEntityToDimension(entity, x, y, z, dimension);
        }
    }

    public static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, double x, double y, double z) {
        player.changeDimension(newDimension, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(newDimension).getDefaultTeleporter());
        if (player.dimension == newDimension) {
            setEntityLocation(player, x, y, z);
            player.velocityChanged = true;
        }
    }

    public static Entity teleportEntityToDimension(Entity entity, double x, double y, double z, int dimension) {
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        WorldServer world = server.getWorld(dimension);
        return teleportEntityToWorld(entity, x, y, z, world);
    }

    public static Entity teleportEntityToWorld(Entity entity, double x, double y, double z, WorldServer newWorld) {
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        WorldServer oldWorld = server.getWorld(entity.dimension);
        if (oldWorld == newWorld) {
            setEntityLocation(entity, x, y, z);
            entity.world.updateEntityWithOptionalForce(entity, false);
            entity.velocityChanged = true; // Have to mark entity velocity changed.
            return entity;
        } else {
            Entity newEntity = entity.changeDimension(newWorld.provider.getDimension(), FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(newWorld.provider.getDimension()).getDefaultTeleporter());
            if (newEntity.dimension == newWorld.provider.getDimension()) {
                setEntityLocation(newEntity, x, y, z);
                newEntity.velocityChanged = true;
            }

            return newEntity;
        }
    }

    private static void setEntityLocation(Entity entity, double x, double y, double z) {
        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
        } else {
            entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        }
    }

}
