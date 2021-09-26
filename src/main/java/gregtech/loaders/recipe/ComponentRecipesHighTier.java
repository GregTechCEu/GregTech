package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

public class ComponentRecipesHighTier {

    public static void register() {

        //Field Generators Start ---------------------------------------------------------------------------------------


        // Motors Start ------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.HSSE)
                .input(OrePrefix.ring, Materials.HSSE, 4)
                .input(OrePrefix.round, Materials.HSSE, 8)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(144))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .duration(600).EUt(10240).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.HSSS)
                .input(OrePrefix.ring, Materials.HSSS, 4)
                .input(OrePrefix.round, Materials.HSSS, 8)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(144))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm())
                .duration(600).EUt(40960).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.Tritanium)
                .input(OrePrefix.ring, Materials.Tritanium, 4)
                .input(OrePrefix.round, Materials.Tritanium, 8)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(144))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .duration(600).EUt(163840).buildAndRegister();
    }
}
