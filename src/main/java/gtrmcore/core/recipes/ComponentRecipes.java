package gtrmcore.core.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.UnificationEntry;

import gtrmcore.common.items.GTRMMetaItems;

import net.minecraft.init.Blocks;

import static gregtech.api.unification.ore.OrePrefix.*;

public class ComponentRecipes {

    public static void init() {
        // circuits
        ModHandler.addShapedRecipe(true, "steam_valve",
                GTRMMetaItems.STEAM_VALVE.getStackForm(), "hGf", "SNS", "FPF",
                'P', new UnificationEntry(pipeNormalFluid, Materials.Potin),
                'F', new UnificationEntry(pipeSmallFluid, Materials.Potin),
                'G', new UnificationEntry(gear, Materials.Bronze),
                'S', new UnificationEntry(gearSmall, Materials.Bronze),
                'N', Blocks.PISTON);
    }
}
