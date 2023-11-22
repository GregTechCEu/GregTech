package gregtech.integration.tinkers.recipe;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Materials;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.shared.TinkerFluids;

public class SmelteryRecipes {

    /** Recipes that are to be added regardless of configs, so that Tinkers and GT play well with each other. */
    public static void registerUnification() {
        // Glass, using the GT fluid amounts
        TinkerRegistry.registerMelting("blockGlass", TinkerFluids.glass, GTValues.L);
        TinkerRegistry.registerMelting("sand", TinkerFluids.glass, GTValues.L);
        TinkerRegistry.registerMelting("dustGlass", TinkerFluids.glass, GTValues.L);
        TinkerRegistry.registerBasinCasting(new ItemStack(Blocks.GLASS), ItemStack.EMPTY, TinkerFluids.glass, GTValues.L);
        TinkerRegistry.registerTableCasting(new ItemStack(Blocks.GLASS_PANE), ItemStack.EMPTY, TinkerFluids.glass, GTValues.L * 3 / 8);

        // Brass, using the GT alloying ratio
        TinkerRegistry.registerAlloy(Materials.Brass.getFluid(4),
                Materials.Zinc.getFluid(1),
                Materials.Copper.getFluid(3));
    }

    public static void registerSmelteryFuels() {
        // Lava: 50L for 100 duration
        // Blaze: 25L for 200 duration (4x better than lava)
        TinkerRegistry.registerSmelteryFuel(Materials.Blaze.getFluid(25), 200);
    }

    public static void registerAlloyingRecipes() {
        // todo some of these fluids are not castable (and their normal materials not meltable)
        TinkerRegistry.registerAlloy(Materials.Cupronickel.getFluid(2),
                Materials.Copper.getFluid(1),
                Materials.Nickel.getFluid(1));

        TinkerRegistry.registerAlloy(Materials.Invar.getFluid(3),
                Materials.Iron.getFluid(2),
                Materials.Nickel.getFluid(1));

        TinkerRegistry.registerAlloy(Materials.BatteryAlloy.getFluid(5),
                Materials.Lead.getFluid(4),
                Materials.Antimony.getFluid(1));

        // Purposefully not registering Tin Alloy because of its relative
        // uselessness vs its ability to conflict in the Smeltery.
    }
}
