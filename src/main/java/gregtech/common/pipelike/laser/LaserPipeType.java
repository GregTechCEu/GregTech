package gregtech.common.pipelike.laser;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum LaserPipeType implements IPipeType<LaserPipeProperties> {

    NORMAL;

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public float getThickness() {
        return 0.375f;
    }

    @Override
    public LaserPipeProperties modifyProperties(LaserPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public PipeModelRedirector getModel() {
        return PipeModelRegistry.getLaserModel();
    }
}
