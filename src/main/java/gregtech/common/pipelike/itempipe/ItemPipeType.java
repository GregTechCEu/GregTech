package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.ore.OrePrefix;

public enum ItemPipeType implements IMaterialPipeType<ItemPipeProperties> {
    //TINY_OPAQUE("tiny", 0.25f, OrePrefix.pipeTinyItem, 0.25f, 2f),
    SMALL("small", 0.375f, OrePrefix.pipeSmallItem, 0.5f, 1.5f),
    NORMAL("normal", 0.5f, OrePrefix.pipeNormalItem, 1f, 1f),
    LARGE("large", 0.75f, OrePrefix.pipeLargeItem, 2f, 0.75f),
    //HUGE_OPAQUE("huge", 0.875f, OrePrefix.pipeHugeItem, 4f, 0.5f);
    RESTRICTIVE_SMALL("small_restrictive", 0.375f, OrePrefix.pipeSmallRestrictive, 0.5f, 150f),
    RESTRICTIVE_NORMAL("normal_restrictive", 0.5f, OrePrefix.pipeNormalRestrictive, 1f, 100f),
    RESTRICTIVE_LARGE("large_restrictive", 0.75f, OrePrefix.pipeLargeRestrictive, 2f, 75f);

    public final String name;
    private final float thickness;
    private final float rateMultiplier;
    private final float resistanceMultiplier;
    private final OrePrefix orePrefix;

    ItemPipeType(String name, float thickness, OrePrefix orePrefix, float rateMultiplier, float resistanceMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.orePrefix = orePrefix;
        this.rateMultiplier = rateMultiplier;
        this.resistanceMultiplier = resistanceMultiplier;
    }

    public boolean isRestrictive() {
        return this == ItemPipeType.RESTRICTIVE_SMALL || this == ItemPipeType.RESTRICTIVE_NORMAL || this == ItemPipeType.RESTRICTIVE_LARGE;
    }

    public String getSizeForTexture() {
        if(!isRestrictive())
            return name;
        else
            return name.substring(0, name.length() - 12);
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public ItemPipeProperties modifyProperties(ItemPipeProperties baseProperties) {
        return new ItemPipeProperties((int) ((baseProperties.priority * resistanceMultiplier) + 0.5), baseProperties.transferRate * rateMultiplier);
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
