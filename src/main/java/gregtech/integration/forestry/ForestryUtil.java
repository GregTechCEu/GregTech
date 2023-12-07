package gregtech.integration.forestry;

import gregtech.api.GTValues;
import gregtech.integration.IntegrationModule;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.bees.GTDropType;

import net.minecraft.item.ItemStack;

import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFlowers;
import forestry.modules.ModuleHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForestryUtil {

    public static boolean apicultureEnabled() {
        return ModuleHelper.isEnabled("apiculture");
    }

    public static boolean arboricultureEnabled() {
        return ModuleHelper.isEnabled("arboriculture");
    }

    public static boolean lepidopterologyEnabled() {
        return ModuleHelper.isEnabled("lepidopterology");
    }

    @Nullable
    public static IAlleleBeeEffect getEffect(@NotNull String modid, @NotNull String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.effect." + name;
            case GTValues.MODID_MB -> "magicbees.effect" + name;
            case GTValues.MODID -> "gregtech.effect." + name;
            default -> "forestry.effect" + name;
        };
        return (IAlleleBeeEffect) AlleleManager.alleleRegistry.getAllele(s);
    }

    @Nullable
    public static IAlleleFlowers getFlowers(@NotNull String modid, @NotNull String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.flower." + name;
            case GTValues.MODID_MB -> "magicbees.flower" + name;
            case GTValues.MODID -> "gregtech.flower." + name;
            default -> "forestry.flowers" + name;
        };
        return (IAlleleFlowers) AlleleManager.alleleRegistry.getAllele(s);
    }

    @Nullable
    public static IAlleleBeeSpecies getSpecies(@NotNull String modid, @NotNull String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.species." + name;
            case GTValues.MODID_MB -> "magicbees.species" + name;
            case GTValues.MODID -> "gregtech.species." + name;
            default -> "forestry.species" + name;
        };
        return (IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(s);
    }

    @NotNull
    public static ItemStack getCombStack(@NotNull GTCombType type) {
        return getCombStack(type, 1);
    }

    @NotNull
    public static ItemStack getCombStack(@NotNull GTCombType type, int amount) {
        if (!ForestryConfig.enableGTBees) {
            IntegrationModule.logger
                    .error("Tried to get GregTech Comb stack, but GregTech Bees config is not enabled!");
            return ItemStack.EMPTY;
        }
        if (!apicultureEnabled()) {
            IntegrationModule.logger.error("Tried to get GregTech Comb stack, but Apiculture module is not enabled!");
            return ItemStack.EMPTY;
        }
        return new ItemStack(ForestryModule.COMBS, amount, type.ordinal());
    }

    @NotNull
    public static ItemStack getDropStack(@NotNull GTDropType type) {
        return getDropStack(type, 1);
    }

    @NotNull
    public static ItemStack getDropStack(@NotNull GTDropType type, int amount) {
        if (!ForestryConfig.enableGTBees) {
            IntegrationModule.logger
                    .error("Tried to get GregTech Drop stack, but GregTech Bees config is not enabled!");
            return ItemStack.EMPTY;
        }
        if (!apicultureEnabled()) {
            IntegrationModule.logger.error("Tried to get GregTech Drop stack, but Apiculture module is not enabled!");
            return ItemStack.EMPTY;
        }
        return new ItemStack(ForestryModule.DROPS, amount, type.ordinal());
    }
}
