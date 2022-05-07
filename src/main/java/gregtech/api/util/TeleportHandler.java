package gregtech.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;

//Uses code from:
//github.com/CleanroomMC/Airlock/blob/master/src/main/java/com/cleanroommc/airlock/common/util/TeleportUtil.java
public class TeleportHandler {

    private TeleportHandler() {
    }

    public static WorldServer getWorldByDimensionID(int id) {
        WorldServer world = DimensionManager.getWorld(id);
        if (world == null) {
            world = DimensionManager.getWorld(0).getMinecraftServer().getWorld(id);
        }
        return world;
    }

    /**
     * Teleport an entity to another entity with a suitable +0.5 offset in positioning
     *
     * @param teleporter entity that is teleporting
     * @param teleportTo entity that is being teleported to
     */
    public static void teleport(Entity teleporter, Entity teleportTo) {
        teleport(teleporter, teleportTo.dimension, teleportTo.posX + 0.5, teleportTo.posY + 0.5, teleportTo.posZ + 0.5);
    }

    /**
     * Teleport an entity to a dimension with provided position
     *
     * @param teleporter entity that is teleporting
     * @param dimension  dimension that the entity is teleporting to
     * @param teleportTo position that the entity is teleporting to
     */
    public static void teleport(Entity teleporter, int dimension, BlockPos teleportTo) {
        teleport(teleporter, dimension, teleportTo.getX(), teleportTo.getY(), teleportTo.getZ());
    }

    /**
     * Teleport an entity to a dimension with provided position
     *
     * @param teleporter  entity that is teleporting
     * @param dimension   dimension that the entity is teleporting to
     * @param teleportToX x position that the entity is teleporting to
     * @param teleportToY y position that the entity is teleporting to
     * @param teleportToZ z position that the entity is teleporting to
     */
    public static void teleport(Entity teleporter, int dimension, double teleportToX, double teleportToY, double teleportToZ) {
        if (teleporter.world.isRemote || teleporter.isDead) {
            return;
        }
        if (teleporter.isBeingRidden()) {
            teleporter.removePassengers();
        }
        if (teleporter.isRiding()) {
            teleporter.dismountRidingEntity();
        }

        if (teleporter.dimension != dimension) {
            // Change dimension
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            // We won't rewrite this method, instead call it since
            // Various Entity implementations can indeed override this
            teleporter.changeDimension(dimension, server.getWorld(dimension).getDefaultTeleporter());
        }
        // Change positions
        teleporter.setPositionAndUpdate(teleportToX, teleportToY, teleportToZ);
    }

    /**
     * Teleport an entity to a dimension with provided position
     *
     * @param teleporter       entity that is teleporting
     * @param dimension        dimension that the entity is teleporting to
     * @param customTeleporter custom teleporter implementation to use instead of dimension's own default one
     * @param teleportToX      x position that the entity is teleporting to
     * @param teleportToY      y position that the entity is teleporting to
     * @param teleportToZ      z position that the entity is teleporting to
     */
    public static void teleport(Entity teleporter, int dimension, ITeleporter customTeleporter, double teleportToX, double teleportToY, double teleportToZ) {
        if (teleporter.world.isRemote || teleporter.isDead) {
            return;
        }
        if (teleporter.isBeingRidden()) {
            teleporter.removePassengers();
        }
        if (teleporter.isRiding()) {
            teleporter.dismountRidingEntity();
        }

        if (teleporter.dimension != dimension) {
            // Change dimension
            // We won't rewrite this method, instead call it since
            // Various Entity implementations can indeed override this
            teleporter.changeDimension(dimension, customTeleporter);
        }
        // Change positions
        teleporter.setPositionAndUpdate(teleportToX, teleportToY, teleportToZ);
    }
}
