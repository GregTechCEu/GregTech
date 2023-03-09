package gregtech.integration.groovy;

import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;

import java.util.ArrayList;
import java.util.List;

public class GroovyMaterialBuilderExpansion {

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
            if (GroovyScriptCompat.validateNonNull(flag, () -> "Can't find material flag for '" + flag + "' in material builder")) {
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
}
