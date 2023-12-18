package gregtech.common.pipelike.laser;

import gregtech.api.pipenet.block.IPipeType;

import org.jetbrains.annotations.NotNull;

public enum LaserPipeType implements IPipeType<LaserPipeProperties> {

    NORMAL;

    @Override
    public @NotNull String getName() {
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
}
