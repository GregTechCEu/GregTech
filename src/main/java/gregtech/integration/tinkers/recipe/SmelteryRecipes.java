package gregtech.integration.tinkers.recipe;

import gregtech.api.unification.material.Materials;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.shared.TinkerFluids;

public class SmelteryRecipes {

    public static void meltingRecipes() {
        // Glass, always done regardless of config
        TinkerRegistry.registerMelting("blockGlass", TinkerFluids.glass, 144);
        TinkerRegistry.registerMelting("sand", TinkerFluids.glass, 144);
        TinkerRegistry.registerMelting("dustGlass", TinkerFluids.glass, 144);
    }

    public static void alloyingRecipes() {
        // Brass, always done regardless of config
        TinkerRegistry.registerAlloy(Materials.Brass.getFluid(4), Materials.Zinc.getFluid(1), Materials.Copper.getFluid(3));
    }

    public static void castingRecipes() {
        // Proper glass casting amounts
        TinkerRegistry.registerBasinCasting(new ItemStack(Blocks.GLASS), ItemStack.EMPTY, TinkerFluids.glass, 144);
        TinkerRegistry.registerTableCasting(new ItemStack(Blocks.GLASS_PANE), ItemStack.EMPTY, TinkerFluids.glass, 54);
    }
}
