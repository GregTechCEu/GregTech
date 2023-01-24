package gregtech.api.unification.crafttweaker;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.info.FluidTypeKey;
import gregtech.api.fluids.info.FluidTypeKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;

import java.util.Arrays;
import java.util.Objects;

public class CTMaterialHelpers {

    protected static ImmutableList<MaterialStack> validateComponentList(MaterialStack[] components) {
        return components == null || components.length == 0 ? ImmutableList.of() : ImmutableList.copyOf(components);
    }

    public static FluidTypeKey validateFluidTypeKey(String typeName) {
        if (typeName == null) return FluidTypeKeys.LIQUID;
        FluidTypeKey key = FluidTypeKeys.getKey(typeName);
        if (key != FluidTypeKeys.LIQUID && key != FluidTypeKeys.GAS && key != FluidTypeKeys.PLASMA) {
            CraftTweakerAPI.logError("FluidTypeKey must be \"liquid\", \"gas\", or \"plasma\"!");
        }
        return key;
    }

    protected static Material[] validateMaterialNames(String methodName, String... names) {
        Material[] materials = Arrays.stream(names).map(GregTechAPI.MaterialRegistry::get).toArray(Material[]::new);
        if (Arrays.stream(materials).anyMatch(Objects::isNull)) {
            logNullMaterial(methodName);
            return null;
        }
        return materials;
    }

    protected static Material validateMaterialName(String name) {
        Material m = GregTechAPI.MaterialRegistry.get(name);
        if (m == null) logBadMaterialName(name);
        return m;
    }

    protected static boolean checkFrozen(String description) {
        if (GregTechAPI.MATERIAL_REGISTRY.isFrozen()) {
            CraftTweakerAPI.logError("Cannot " + description + " now, must be done in a file labeled with \"#loader gregtech\"");
            return true;
        } return false;
    }

    protected static void logError(Material m, String cause, String type) {
        CraftTweakerAPI.logError("Cannot " + cause + " of a Material with no " + type + "! Try calling \"add" + type + "\" in your \"#loader gregtech\" file first if this is intentional. Material: " + m.getUnlocalizedName());
    }

    protected static void logPropertyExists(Material m, String propName) {
        CraftTweakerAPI.logWarning("Material " + m.getUnlocalizedName() + " has " + propName + " already. Skipping...");
    }

    protected static void logBadMaterialName(String name) {
        CraftTweakerAPI.logError("Material with name " + name + " does not exist! If this is a Material added by CT, try passing the Material directly instead of as a String");
    }

    protected static void logNullMaterial(String methodName) {
        CraftTweakerAPI.logError("Null Material passed to Builder method " + methodName + "!");
    }
}
