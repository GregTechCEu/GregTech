package gtqt.api.util;

import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//from https://github.com/MCTian-mi/SussyPatches/blob/main/src/main/java/dev/tianmi/sussypatches/common/helper/ChunkAwareHook.java
public class ChunkAwareHook {

    @SubscribeEvent
    public static void onChunkLoadUnload(ChunkEvent event) {
        boolean isLoad = event instanceof ChunkEvent.Load;
        boolean isUnLoad = event instanceof ChunkEvent.Unload;
        if (!isLoad && !isUnLoad) return;
        var world = event.getWorld();
        var chunk = event.getChunk();
        int x = chunk.x, z = chunk.z;

        var chunkProvider = world.getChunkProvider();
        for (var side : EnumFacing.HORIZONTALS) {
            var nearbyChunk = chunkProvider.getLoadedChunk(x + side.getXOffset(), z + side.getZOffset());
            if (nearbyChunk != null && nearbyChunk.isLoaded()) {
                nearbyChunk.getTileEntityMap().forEach((pos, tile) -> {
                    if (tile instanceof NeighborCacheTileEntityBase chunkAware && isNextToChunkAtSide(pos, x, z, side)) {
                        if (isLoad) chunkAware.onNeighborChunkLoad(side.getOpposite());
                        if (isUnLoad) chunkAware.onNeighborChunkUnload(side.getOpposite());
                    }
                });
            }
        }
    }

    private static boolean isNextToChunkAtSide(BlockPos pos, int chunkX, int chunkZ, EnumFacing side) {
        int minX = chunkX * 16, minZ = chunkZ * 16;
        return switch (side) {
            case EAST -> pos.getX() - minX == -1;
            case WEST -> pos.getX() - minX == 16;
            case NORTH -> pos.getZ() - minZ == -1;
            case SOUTH -> pos.getZ() - minZ == 16;
            default -> false; // Should never reach here;
        };
    }
}
