package gregtech.integration.groovy;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.ExtraToolProperty;
import gregtech.api.unification.material.properties.MaterialToolProperty;
import gregtech.api.unification.stack.MaterialStack;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.groovyscript.api.GroovyLog;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
        BlastProperty.GasTier gasTier = GroovyScriptModule.parseAndValidateEnumValue(BlastProperty.GasTier.class, raw,
                "gas tier");
        if (gasTier != null) {
            return builder.blast(b -> b
                    .temp(temp, gasTier)
                    .blastStats(eutOverride, durationOverride)
                    .vacuumStats(vacuumEUtOverride, vacuumDurationOverride));
        }
        return builder;
    }

    public static Material.Builder components(Material.Builder builder, Object... objects) {
        ObjectArrayList<MaterialStack> materialStacks = new ObjectArrayList<>();
        for (Object o : objects) {
            if (o instanceof MaterialStack materialStack) {
                materialStacks.add(materialStack);
            } else if (o instanceof Material material) {
                materialStacks.add(new MaterialStack(material, 1));
            } else if (o instanceof Integer) {
                GroovyLog.msg("Error creating GregTech material")
                        .add("Tried to use old method for material components in the shape of (material1, amount1, material2, amount2)")
                        .add("Please change this into (material1 * amount1, material2 * amount2)")
                        .error().post();
            } else {
                GroovyLog.msg("Error creating GregTech material")
                        .add("Material components must be of type Material or MaterialStack, but was of type {}",
                                o == null ? null : o.getClass())
                        .error().post();
            }
        }
        return builder.components(materialStacks.toArray(new MaterialStack[0]));
    }

    public static Material.Builder toolStats(Material.Builder builder, MaterialToolProperty.Builder toolBuilder) {
        return builder.toolStats(toolBuilder.build());
    }

    public static Material.Builder toolStats(Material.Builder builder, float harvestSpeed, float attackDamage,
                                             int durability, int harvestLevel) {
        return builder.toolStats(
                MaterialToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel).build());
    }

    public static Material.Builder overrideToolStats(Material.Builder builder, String toolId,
                                                     ExtraToolProperty.Builder overrideBuilder) {
        return builder.overrideToolStats(toolId, overrideBuilder.build());
    }
}
