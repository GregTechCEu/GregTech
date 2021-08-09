package gregtech.common.pipelike.fluidpipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.ore.OrePrefix;

import javax.annotation.Nonnull;

public enum FluidPipeType implements IMaterialPipeType<FluidPipeProperties> {

    TINY_OPAQUE("tiny", 0.25f, 1, OrePrefix.pipeTinyFluid, true),
    SMALL_OPAQUE("small", 0.375f, 2, OrePrefix.pipeSmallFluid, true),
    NORMAL_OPAQUE("normal", 0.5f, 6, OrePrefix.pipeNormalFluid, true),
    LARGE_OPAQUE("large", 0.75f, 12, OrePrefix.pipeLargeFluid, true),
    HUGE_OPAQUE("huge", 0.875f, 24, OrePrefix.pipeHugeFluid, true);

    public final String name;
    public final float thickness;
    public final int capacityMultiplier;
    public final OrePrefix orePrefix;
    public final boolean opaque;

    FluidPipeType(String name, float thickness, int capacityMultiplier, OrePrefix orePrefix, boolean opaque) {
        this.name = name;
        this.thickness = thickness;
        this.capacityMultiplier = capacityMultiplier;
        this.orePrefix = orePrefix;
        this.opaque = opaque;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }

    @Override
    public FluidPipeProperties modifyProperties(FluidPipeProperties baseProperties) {
        return new FluidPipeProperties(
                baseProperties.maxFluidTemperature,
                baseProperties.throughput * capacityMultiplier,
                baseProperties.gasProof);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
