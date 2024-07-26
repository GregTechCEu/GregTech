package gregtech.common.pipelike.block.optical;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.renderer.pipe.ActivablePipeModel;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

@Desugar
public record OpticalStructure(String name, float renderThickness, ActivablePipeModel model) implements IPipeStructure {

    public static final OpticalStructure INSTANCE = new OpticalStructure("standard", 0.375f, ActivablePipeModel.OPTICAL);

    @Override
    public boolean canConnectTo(EnumFacing side, byte connectionMask) {
        byte connectionCount = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
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
}
