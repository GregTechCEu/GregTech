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
    private final float rateMultiplier;
    private final OrePrefix orePrefix;

    ItemPipeType(String name, float thickness, OrePrefix orePrefix, float rateMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.orePrefix = orePrefix;
        this.rateMultiplier = rateMultiplier;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public ItemPipeProperties modifyProperties(ItemPipeProperties baseProperties) {
        return baseProperties;
    }

    public float getRateMultiplier() {
        return rateMultiplier;
    }

    @Override
    public boolean isPaintable() {
        return true;
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
