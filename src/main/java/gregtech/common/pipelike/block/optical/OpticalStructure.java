package gregtech.common.pipelike.block.optical;

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
public record OpticalStructure(String name, float renderThickness, ActivablePipeModel model) implements IPipeStructure {

    public static final OpticalStructure INSTANCE = new OpticalStructure("optical_pipe_normal", 0.375f,
            ActivablePipeModel.OPTICAL);

    public OpticalStructure(String name, float renderThickness, ActivablePipeModel model) {
        this.name = name;
        this.renderThickness = renderThickness;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

    @Override
    public boolean canConnectTo(EnumFacing side, byte connectionMask) {
        byte connectionCount = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == side) continue;
            if (GTUtility.evalMask(facing, connectionMask)) {
                connectionCount++;
            }
            if (connectionCount > 1) return false;
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

    public static void registerDefaultStructures(Consumer<OpticalStructure> register) {
        register.accept(INSTANCE);
    }
}
