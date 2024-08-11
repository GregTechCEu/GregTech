package gregtech.common.pipelike.block.laser;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistry;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraft.util.EnumFacing;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Desugar
public record LaserStructure(String name, float renderThickness, boolean mirror, ActivablePipeModel model)
        implements IPipeStructure {

    public static final LaserStructure NORMAL = new LaserStructure("laser_pipe_normal", 0.375f,
            false, ActivablePipeModel.LASER);
    public static final LaserStructure MIRROR = new LaserStructure("laser_pipe_mirror", 0.5f,
            true, ActivablePipeModel.LASER);

    public LaserStructure(String name, float renderThickness, boolean mirror, ActivablePipeModel model) {
        this.name = name;
        this.renderThickness = renderThickness;
        this.mirror = mirror;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

    @Override
    public boolean canConnectTo(EnumFacing side, byte connectionMask) {
        if (mirror) {
            byte connectionCount = 0;
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing == side) continue;
                if (GTUtility.evalMask(facing, connectionMask)) {
                    if (facing.getOpposite() == side) return false; // must be a bent connection
                    connectionCount++;
                }
                if (connectionCount > 1) return false;
            }
        } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (facing == side) continue;
                if (GTUtility.evalMask(facing, connectionMask)) {
                    return facing.getOpposite() == side;
                }
            }
        }
        return true;
    }

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
        return true;
    }

    @Override
    public AbstractPipeModel<?> getModel() {
        return model;
    }

    public static void registerDefaultStructures(Consumer<LaserStructure> register) {
        register.accept(NORMAL);
    }
}
