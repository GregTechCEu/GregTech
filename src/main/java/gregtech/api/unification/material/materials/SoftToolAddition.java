package gregtech.api.unification.material.materials;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.ExtraToolProperty;
import gregtech.api.unification.material.properties.PropertyKey;

public class SoftToolAddition {

    public static final Material[] softMaterials = new Material[] {
            Materials.Wood, Materials.Rubber, Materials.Polyethylene,
            Materials.Polytetrafluoroethylene, Materials.Polybenzimidazole
    };

    public static void register() {
        for (int i = 0; i < softMaterials.length; i++) {

            Material material = softMaterials[i];

            if (ModHandler.isMaterialWood(material)) {
                material.getProperties().ensureSet(PropertyKey.EXTRATOOL);
                ExtraToolProperty extraToolProperty = material.getProperty(PropertyKey.EXTRATOOL);

                extraToolProperty.setOverrideProperty(ToolClasses.SOFT_MALLET,
                        ExtraToolProperty.Builder.of(4F, 1F, 48, 1).build());
            } else {
                material.getProperties().ensureSet(PropertyKey.EXTRATOOL);
                ExtraToolProperty extraToolProperty = material.getProperty(PropertyKey.EXTRATOOL);

                extraToolProperty.setOverrideProperty(ToolClasses.SOFT_MALLET,
                        ExtraToolProperty.Builder.of(4F, 1F, 128 * (1 << i), 1).build());
                extraToolProperty.setOverrideProperty(ToolClasses.PLUNGER,
                        ExtraToolProperty.Builder.of(4F, 0F, 128 * (1 << i), 1).build());
            }
        }
    }
}
