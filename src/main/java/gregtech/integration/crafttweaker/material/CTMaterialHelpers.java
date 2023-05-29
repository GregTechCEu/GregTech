package gregtech.integration.crafttweaker.material;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialHelpers;
import gregtech.api.unification.material.registry.MaterialRegistryManager;
import gregtech.api.unification.stack.MaterialStack;

import java.util.Arrays;
import java.util.Objects;

public class CTMaterialHelpers {

    protected static ImmutableList<MaterialStack> validateComponentList(MaterialStack[] components) {
        return components == null || components.length == 0 ? ImmutableList.of() : ImmutableList.copyOf(components);
    }

    protected static FluidType validateFluidType(String fluidTypeName) {
        if (fluidTypeName == null || fluidTypeName.equals("fluid"))
            return FluidTypes.LIQUID;

        FluidType type = FluidType.getByName(fluidTypeName);
        if (type == null) {
            String message = "Fluid Type must be either \"liquid\", \"gas\", \"plasma\", or \"acid\"!";
            CraftTweakerAPI.logError(message);
            throw new IllegalArgumentException(message);
        }
        return type;
    }

    protected static FluidType validateFluidTypeNoPlasma(String fluidTypeName) {
        if (fluidTypeName == null || fluidTypeName.equals("fluid"))
            return FluidTypes.LIQUID;

        FluidType type = FluidType.getByName(fluidTypeName);
        if (type == null) {
            String message = "Fluid Type must be either \"liquid\", \"gas\", or \"acid\"!";
            CraftTweakerAPI.logError(message);
            throw new IllegalArgumentException(message);
        }
        if (type == FluidTypes.PLASMA) {
            String message = "Fluid Type cannot be \"plasma\". Use the plasma method instead.";
            CraftTweakerAPI.logError(message);
            throw new IllegalArgumentException(message);
        }
        return type;
    }

    protected static Material[] validateMaterialNames(String methodName, String... names) {
        Material[] materials = Arrays.stream(names).map(MaterialHelpers::getMaterial).toArray(Material[]::new);
        if (Arrays.stream(materials).anyMatch(Objects::isNull)) {
            logNullMaterial(methodName);
            return null;
        }
        return materials;
    }

    protected static Material validateMaterialName(String name) {
        Material m = MaterialHelpers.getMaterial(name);
        if (m == null) logBadMaterialName(name);
        return m;
    }

    protected static boolean checkFrozen(String description) {
        if (MaterialRegistryManager.canModifyMaterials()) {
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
