package gregtech.common.pipelike.optical;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public enum OpticalPipeType implements IPipeType<OpticalPipeProperties> {

    NORMAL;

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public OpticalPipeProperties modifyProperties(OpticalPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @NotNull
    @Override
    public String getName() {
        return "normal";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PipeModelRedirector getModel() {
        return PipeModelRegistry.getOpticalModel();
    }
}
