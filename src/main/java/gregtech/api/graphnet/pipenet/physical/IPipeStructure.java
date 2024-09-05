package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.client.renderer.pipe.PipeModelRedirector;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IPipeStructure extends IStringSerializable {

    /**
     * Used as reference for misc things, e.g. rendering the backing of a cover.
     * 
     * @return render thickness
     */
    float getRenderThickness();

    boolean isPaintable();

    PipeModelRedirector getModel();

    /**
     * Allows for controlling what sides can be connected to based on current connections,
     * such as in the case of optical and laser pipes.
     */
    default boolean canConnectTo(EnumFacing side, byte connectionMask) {
        return true;
    }

    @Contract("_ -> new")
    default List<AxisAlignedBB> getPipeBoxes(@NotNull PipeTileEntity tileContext) {
        List<AxisAlignedBB> pipeBoxes = new ObjectArrayList<>();
        float thickness = getRenderThickness();
        if ((tileContext.getCoverAdjustedConnectionMask() & 63) < 63) {
            pipeBoxes.add(getSideBox(null, thickness));
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (tileContext.isConnectedCoverAdjusted(facing))
                pipeBoxes.add(getSideBox(facing, thickness));
        }
        return pipeBoxes;
    }

    static AxisAlignedBB getSideBox(EnumFacing side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0f, faceMax = 1f;

        if (side == null)
            return new AxisAlignedBB(min, min, min, max, max, max);
        return switch (side) {
            case WEST -> new AxisAlignedBB(faceMin, min, min, min, max, max);
            case EAST -> new AxisAlignedBB(max, min, min, faceMax, max, max);
            case NORTH -> new AxisAlignedBB(min, min, faceMin, max, max, min);
            case SOUTH -> new AxisAlignedBB(min, min, max, max, max, faceMax);
            case UP -> new AxisAlignedBB(min, max, min, max, faceMax, max);
            case DOWN -> new AxisAlignedBB(min, faceMin, min, max, min, max);
        };
    }
}
