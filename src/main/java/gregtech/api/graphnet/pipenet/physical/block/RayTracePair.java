package gregtech.api.graphnet.pipenet.physical.block;

import com.github.bsideup.jabel.Desugar;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

@Desugar
public record RayTracePair(RayTraceResult result, AxisAlignedBB bb) {}
