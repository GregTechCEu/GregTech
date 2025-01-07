package gregtech.common.pipelike.block.warp;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Desugar
public record WarpDuctStructure(String name, int weight, float renderThickness, PipeModelRedirector model) implements
        IPipeStructure {

    public static final WarpDuctStructure NORMAL = new WarpDuctStructure("warp_duct_normal", 1, 0.625f,
            PipeModelRegistry.getWarpDuctModel(false));

    public static final WarpDuctStructure RESTRICTIVE = new WarpDuctStructure("warp_duct_restrictive", 1000, 0.625f,
            PipeModelRegistry.getWarpDuctModel(true));

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public boolean isPaintable() {
        return false;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }

    @Override
    public @NotNull AxisAlignedBB getSideBox(EnumFacing side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0.005f, faceMax = 0.995f;

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

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(NORMAL);
        event.register(RESTRICTIVE);
    }
}
