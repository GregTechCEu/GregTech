package gregtech.api.pollution;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;

final class PollutionMap {

    private static final int RADIUS = 24;
    private static final int RELOAD_DISTANCE = 5;
    private static final int SIZE = RADIUS * 2 + 1;
    private final short[][] chunkMatrix = new short[SIZE][SIZE];
    private int centerX;
    private int centerZ;
    private boolean initialized;

    void addPollution(int chunkX, int chunkZ, int pollution) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || player.world == null) return;

        int playerChunkX = MathHelper.floor(player.posX) >> 4;
        int playerChunkZ = MathHelper.floor(player.posZ) >> 4;

        if (!initialized) {
            this.centerX = playerChunkX;
            this.centerZ = playerChunkZ;
            this.initialized = true;
        }

        if (Math.abs(centerX - playerChunkX) > RELOAD_DISTANCE || Math.abs(centerZ - playerChunkZ) > RELOAD_DISTANCE) {
            shiftCenter(playerChunkX, playerChunkZ);
        }

        int relX = chunkX - centerX + RADIUS;
        if (relX < 0 || relX >= SIZE) return;
        int relZ = chunkZ - centerZ + RADIUS;
        if (relZ < 0 || relZ >= SIZE) return;

        chunkMatrix[relX][relZ] = (short) MathHelper.clamp(pollution / 255, 0, Short.MAX_VALUE);
    }

    /**
     * Shift the pollution render matrix to a new center point
     *
     * @param chunkX the new center X coordinate
     * @param chunkZ the new center Z coordinate
     */
    private void shiftCenter(int chunkX, int chunkZ) {
        int dX = chunkX - centerX;
        int dZ = chunkZ - centerZ;
        long empties = 0;

        if (dX > 0) {
            for (int x = 0; x < SIZE; x++) {
                int xOffset = x + dX;
                if (xOffset < SIZE) {
                    chunkMatrix[x] = chunkMatrix[xOffset].clone();
                } else {
                    chunkMatrix[x] = new short[x];
                    empties |= 1L << x;
                }
            }
        } else if (dX < 0) {
            for (int x = SIZE - 1; x >= 0; x--) {
                int xOffset = x + dX;
                if (xOffset > 0) {
                    chunkMatrix[x] = chunkMatrix[xOffset].clone();
                } else {
                    chunkMatrix[x] = new short[x];
                    empties |= 1L << x;
                }
            }
        }

        if (dZ > 0) {
            for (int x = 0; x < SIZE; x++) {
                if ((empties & (1L << x)) != (1L << x)) continue;
                for (int z = 0; z < SIZE; z++) {
                    int zOffset = z + dZ;
                    chunkMatrix[x][z] = zOffset < SIZE ? chunkMatrix[x][zOffset] : 0;
                }
            }
        } else if (dZ < 0) {
            for (int x = 0; x < SIZE; x++) {
                if ((empties & (1L << x)) != (1L << x)) continue;
                for (int z = SIZE - 1; z >= 0; z--) {
                    int zOffset = z + dZ;
                    chunkMatrix[x][z] = zOffset > 0 ? chunkMatrix[x][zOffset] : 0;
                }
            }
        }

        centerX = chunkX;
        centerZ = chunkZ;
    }

    /**
     * Get the pollution stored at a block's coordinates
     *
     * @param x the x block coordinate
     * @param z the z block coordinate
     * @return the pollution at the coordinate
     */
    int getPollution(int x, int z) {
        if (!initialized) {
            return 0;
        }

        int dX = ((x - 8) >> 4) - centerX;
        int dZ = ((z - 8) >> 4) - centerZ;

        int offsetX = RADIUS + dX;
        int offsetZ = RADIUS + dZ;

        if (offsetX < 0 || offsetZ < 0 || offsetX + 1 >= SIZE || offsetZ + 1 >= SIZE) {
            return 0;
        }

        x = (x - 8) % 16;
        z = (z - 8) % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;

        int xi = 15 - x;
        int zi = 15 - z;

        return (chunkMatrix[offsetX][offsetZ] * xi * zi) + (chunkMatrix[offsetX + 1][offsetZ] * x * zi) +
                (chunkMatrix[offsetX][offsetZ + 1] * xi * z) + (chunkMatrix[offsetX + 1][offsetZ + 1] * x * z);
    }
}
