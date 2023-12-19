package gregtech.api.cover;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.util.GTUtility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.RayTracer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CoverRayTracer {

    private CoverRayTracer() {}

    public static @Nullable EnumFacing rayTraceCoverableSide(@NotNull CoverableView coverableView,
                                                             @NotNull EntityPlayer player) {
        // if the coverable view is from a blockpipe, use the proper raytrace method
        RayTraceResult result = coverableView.getWorld().getBlockState(coverableView.getPos())
                .getBlock() instanceof BlockPipe<?, ?, ?>pipe ?
                        pipe.getServerCollisionRayTrace(player, coverableView.getPos(), coverableView.getWorld()) :
                        RayTracer.retraceBlock(coverableView.getWorld(), player, coverableView.getPos());
        if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }

        return traceCoverSide(result);
    }

    public static @Nullable EnumFacing traceCoverSide(@NotNull RayTraceResult result) {
        if (result instanceof CuboidRayTraceResult rayTraceResult) {
            if (rayTraceResult.cuboid6.data == null) {
                return determineGridSideHit(result);
            } else if (rayTraceResult.cuboid6.data instanceof CoverSideData coverSideData) {
                return coverSideData.side;
            } else if (rayTraceResult.cuboid6.data instanceof BlockPipe.PipeConnectionData pipeConnectionData) {
                return pipeConnectionData.side;
            } else if (rayTraceResult.cuboid6.data instanceof PrimaryBoxData primaryBoxData) {
                return primaryBoxData.usePlacementGrid ? determineGridSideHit(result) : result.sideHit;
            } // unknown hit type, fall through
        }
        // normal collision ray trace, return side hit
        return determineGridSideHit(result);
    }

    public static @Nullable EnumFacing determineGridSideHit(@NotNull RayTraceResult result) {
        return GTUtility.determineWrenchingSide(result.sideHit,
                (float) (result.hitVec.x - result.getBlockPos().getX()),
                (float) (result.hitVec.y - result.getBlockPos().getY()),
                (float) (result.hitVec.z - result.getBlockPos().getZ()));
    }

    public static class PrimaryBoxData {

        public final boolean usePlacementGrid;

        public PrimaryBoxData(boolean usePlacementGrid) {
            this.usePlacementGrid = usePlacementGrid;
        }
    }

    public static class CoverSideData {

        public final EnumFacing side;

        public CoverSideData(EnumFacing side) {
            this.side = side;
        }
    }
}
