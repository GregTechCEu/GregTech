package gregtech.common.pipelike.itempipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.unification.ore.OrePrefix;

import org.jetbrains.annotations.NotNull;

public enum ItemPipeType implements IMaterialPipeType<ItemPipeProperties> {

    // TINY_OPAQUE("tiny", 0.25f, OrePrefix.pipeTinyItem, 0.25f, 2f),
    NORMAL("normal", 0.5f, OrePrefix.pipeItem, 1f, 1f),
    RESTRICTIVE_NORMAL("normal_restrictive", 0.5f, OrePrefix.pipeRestrictive, 1f, 100f);

    public static final ItemPipeType[] VALUES = values();

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
        return ordinal() > 3;
    }

    public String getSizeForTexture() {
        if (!isRestrictive())
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
        return new ItemPipeProperties((int) ((baseProperties.getPriority() * resistanceMultiplier) + 0.5),
                baseProperties.getTransferRate() * rateMultiplier);
    }

    public float getRateMultiplier() {
        return rateMultiplier;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }
}
