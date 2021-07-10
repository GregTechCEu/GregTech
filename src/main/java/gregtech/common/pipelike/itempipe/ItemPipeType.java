package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.ore.OrePrefix;

public enum ItemPipeType implements IMaterialPipeType<ItemPipeProperties> {
    TINY_OPAQUE("tiny", 0.25f, OrePrefix.pipeTinyItem, 0.25f),
    SMALL_OPAQUE("small", 0.375f, OrePrefix.pipeSmallItem, 0.5f),
    MEDIUM_OPAQUE("medium", 0.5f, OrePrefix.pipeMediumItem, 1f),
    LARGE_OPAQUE("large", 0.75f, OrePrefix.pipeLargeItem, 2f),
    HUGE_OPAQUE("huge", 0.875f, OrePrefix.pipeHugeItem, 4f);

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
