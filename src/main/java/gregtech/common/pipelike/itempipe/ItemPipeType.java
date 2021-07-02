package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.unification.ore.OrePrefix;

public enum ItemPipeType implements IMaterialPipeType<EmptyNodeData> {
    TINY_OPAQUE("tiny", 0.25f, OrePrefix.pipeTiny, true),
    SMALL_OPAQUE("small", 0.375f, OrePrefix.pipeSmall, true),
    MEDIUM_OPAQUE("medium", 0.5f, OrePrefix.pipeMedium, true),
    LARGE_OPAQUE("large", 0.75f, OrePrefix.pipeLarge, true),
    HUGE_OPAQUE("huge", 0.875f, OrePrefix.pipeHuge, true);

    public final String name;
    private final float thickness;
    private final boolean isPaintable;
    private final OrePrefix orePrefix;

    ItemPipeType(String name, float thickness, OrePrefix orePrefix, boolean isPaintable) {
        this.name = name;
        this.thickness = thickness;
        this.orePrefix = orePrefix;
        this.isPaintable = isPaintable;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public EmptyNodeData modifyProperties(EmptyNodeData baseProperties) {
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return isPaintable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }
}
