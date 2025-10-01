package gregtech.api.block.coil;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.model.ActiveVariantBlockBakedModel;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class CustomCoilStats implements IHeatingCoilBlockStats, Comparable<CustomCoilStats>,
                                   IStringSerializable {

    String name;

    // electric blast furnace properties
    int coilTemperature = -1;

    // multi smelter properties
    int level = -1;
    int energyDiscount = 0;

    // voltage tier
    int tier = GTValues.ULV;

    Material material = Materials.Iron;

    CustomCoilStats() {}

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getCoilTemperature() {
        return coilTemperature;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnergyDiscount() {
        return energyDiscount;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public @Nullable Material getMaterial() {
        return material;
    }

    @Override
    public ActiveVariantBlockBakedModel createModel(Predicate<Object> bloomConfig) {
        return null;
    }

    @Override
    public int compareTo(@NotNull CustomCoilStats o) {
        // todo add more comparisons?
        return Integer.compare(o.getTier(), this.getTier());
    }
}
