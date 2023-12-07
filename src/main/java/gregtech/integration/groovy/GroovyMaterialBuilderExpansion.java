package gregtech.integration.groovy;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.gregtechId;

public class GroovyMaterialBuilderExpansion {

    public static Material.Builder fluid(Material.Builder builder, String raw, FluidBuilder fluidBuilder) {
        FluidStorageKey key = FluidStorageKey.getByName(new ResourceLocation(raw));
        if (key == null) key = FluidStorageKey.getByName(gregtechId(raw));
        if (GroovyScriptModule.validateNonNull(key,
                () -> "Can't find fluid type for " + raw + " in material builder")) {
            return builder.fluid(key, fluidBuilder);
        }
        return builder;
    }

    public static Material.Builder gas(Material.Builder builder, int temp) {
        return builder.gas(new FluidBuilder().temperature(temp));
    }

    public static Material.Builder liquid(Material.Builder builder, int temp) {
        return builder.liquid(new FluidBuilder().temperature(temp));
    }

    public static Material.Builder plasma(Material.Builder builder, int temp) {
        return builder.plasma(new FluidBuilder().temperature(temp));
    }

    public static Material.Builder element(Material.Builder builder, String raw) {
        Element element = Elements.get(raw);
        if (GroovyScriptModule.validateNonNull(element,
                () -> "Can't find element for " + raw + " in material builder")) {
            return builder.element(element);
        }
        return builder;
    }

    public static Material.Builder flags(Material.Builder builder, String... rawFlags) {
        List<MaterialFlag> flags = new ArrayList<>();
        for (String rawFlag : rawFlags) {
            MaterialFlag flag = MaterialFlag.getByName(rawFlag);
            if (GroovyScriptModule.validateNonNull(flag,
                    () -> "Can't find material flag for '" + rawFlag + "' in material builder")) {
                flags.add(flag);
            }
        }
        return builder.flags(flags);
    }

    public static Material.Builder iconSet(Material.Builder builder, String raw) {
        MaterialIconSet iconSet = MaterialIconSet.getByName(raw);
        if (GroovyScriptModule.validateNonNull(iconSet,
                () -> "Can't find material icon set for " + raw + " in material builder")) {
            return builder.iconSet(iconSet);
        }
        return builder;
    }

    public static Material.Builder blastTemp(Material.Builder builder, int temp, String raw) {
        return blastTemp(builder, temp, raw, -1, -1);
    }

    public static Material.Builder blastTemp(Material.Builder builder, int temp, String raw, int eutOverride) {
        return blastTemp(builder, temp, raw, eutOverride, -1);
    }

    public static Material.Builder blastTemp(Material.Builder builder, int temp, String raw, int eutOverride,
                                             int durationOverride) {
        return blastTemp(builder, temp, raw, eutOverride, durationOverride, -1, -1);
    }

    public static Material.Builder blastTemp(Material.Builder builder, int temp, String raw, int eutOverride,
                                             int durationOverride, int vacuumEUtOverride, int vacuumDurationOverride) {
        BlastProperty.GasTier gasTier = null;
        String name = raw.toUpperCase();
        for (BlastProperty.GasTier gasTier1 : BlastProperty.GasTier.VALUES) {
            if (gasTier1.name().equals(name)) {
                gasTier = gasTier1;
                break;
            }
        }
        final BlastProperty.GasTier finalGasTier = gasTier;
        if (GroovyScriptModule.validateNonNull(gasTier, () -> "Can't find gas tier for " + name +
                " in material builder. Valid values are 'low', 'mid', 'high', 'higher', 'highest'!")) {
            return builder.blast(b -> b
                    .temp(temp, finalGasTier)
                    .blastStats(eutOverride, durationOverride)
                    .vacuumStats(vacuumEUtOverride, vacuumDurationOverride));
        }
        return builder;
    }
}
