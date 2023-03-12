package gregtech.integration.groovy;

import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;

import java.util.ArrayList;
import java.util.List;

public class GroovyMaterialBuilderExpansion {

    public static Material.Builder fluid(Material.Builder builder, String raw) {
        return fluid(builder, raw, false);
    }

    public static Material.Builder fluid(Material.Builder builder, String raw, boolean hasBlock) {
        FluidType fluidType = FluidType.getByName(raw);
        if (GroovyScriptCompat.validateNonNull(fluidType, () -> "Can't find fluid type for " + raw + " in material builder")) {
            return builder.fluid(fluidType, hasBlock);
        }
        return builder;
    }

    public static Material.Builder element(Material.Builder builder, String raw) {
        Element element = Elements.get(raw);
        if (GroovyScriptCompat.validateNonNull(element, () -> "Can't find element for " + raw + " in material builder")) {
            return builder.element(element);
        }
        return builder;
    }

    public static Material.Builder flags(Material.Builder builder, String... rawFlags) {
        List<MaterialFlag> flags = new ArrayList<>();
        for (String rawFlag : rawFlags) {
            MaterialFlag flag = MaterialFlag.getByName(rawFlag);
            if (GroovyScriptCompat.validateNonNull(flag, () -> "Can't find material flag for '" + rawFlag + "' in material builder")) {
                flags.add(flag);
            }
        }
        return builder.flags(flags);
    }

    public static Material.Builder iconSet(Material.Builder builder, String raw) {
        MaterialIconSet iconSet = MaterialIconSet.getByName(raw);
        if (GroovyScriptCompat.validateNonNull(iconSet, () -> "Can't find material icon set for " + raw + " in material builder")) {
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

    public static Material.Builder blastTemp(Material.Builder builder, int temp, String raw, int eutOverride, int durationOverride) {
        BlastProperty.GasTier gasTier = null;
        String name = raw.toUpperCase();
        for (BlastProperty.GasTier gasTier1 : BlastProperty.GasTier.VALUES) {
            if (gasTier1.name().equals(name)) {
                gasTier = gasTier1;
                break;
            }
        }
        if (GroovyScriptCompat.validateNonNull(gasTier, () -> "Can't find gas tier for " + name + " in material builder. Valid values are 'low', 'mid', 'high', 'higher', 'highest'!")) {
            return builder.blastTemp(temp, gasTier, eutOverride, durationOverride);
        }
        return builder;
    }
}
