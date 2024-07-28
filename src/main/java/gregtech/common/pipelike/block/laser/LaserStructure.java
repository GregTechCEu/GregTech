package gregtech.common.pipelike.block.laser;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.renderer.pipe.ActivablePipeModel;

import gregtech.common.pipelike.block.optical.OpticalStructure;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Desugar
public record LaserStructure(String name, float renderThickness, ActivablePipeModel model) implements IPipeStructure {

    public static final LaserStructure INSTANCE = new LaserStructure("standard", 0.375f, ActivablePipeModel.LASER);

    @Override
    public boolean canConnectTo(EnumFacing side, byte connectionMask) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (GTUtility.evalMask(facing, connectionMask)) {
                return facing.getOpposite() == side;
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
        register.accept(INSTANCE);
    }
}
