package gregtech.integration.crafttweaker.material;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidState;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;

import com.google.common.collect.ImmutableList;
import crafttweaker.CraftTweakerAPI;

public class CTMaterialHelpers {

    protected static ImmutableList<MaterialStack> validateComponentList(MaterialStack[] components) {
        return components == null || components.length == 0 ? ImmutableList.of() : ImmutableList.copyOf(components);
    }

    protected static FluidState validateFluidState(String fluidTypeName) {
        if (fluidTypeName == null || fluidTypeName.equals("fluid"))
            return FluidState.LIQUID;

        if (fluidTypeName.equals("liquid")) return FluidState.LIQUID;
        if (fluidTypeName.equals("gas")) return FluidState.GAS;
        if (fluidTypeName.equals("plasma")) return FluidState.PLASMA;

        String message = "Fluid Type must be either \"liquid\", \"gas\", or \"acid\"!";
        CraftTweakerAPI.logError(message);
        throw new IllegalArgumentException(message);
    }

    protected static boolean checkFrozen(String description) {
        if (!GregTechAPI.materialManager.canModifyMaterials()) {
            CraftTweakerAPI.logError(
                    "Cannot " + description + " now, must be done in a file labeled with \"#loader gregtech\"");
            return true;
        }
        return false;
    }

    protected static void logError(Material m, String cause, String type) {
        CraftTweakerAPI.logError("Cannot " + cause + " of a Material with no " + type + "! Try calling \"add" + type +
                "\" in your \"#loader gregtech\" file first if this is intentional. Material: " +
                m.getUnlocalizedName());
    }
}
