package gregtech.common.pipelike.block.optical;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import net.minecraft.util.EnumFacing;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@Desugar
public record OpticalStructure(String name, float renderThickness, PipeModelRedirector model)
        implements IPipeStructure {

    public static final OpticalStructure INSTANCE = new OpticalStructure("optical_pipe_normal", 0.375f,
            PipeModelRegistry.getOpticalModel());

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
    public PipeModelRedirector getModel() {
        return model;
    }

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(INSTANCE);
    }
}
