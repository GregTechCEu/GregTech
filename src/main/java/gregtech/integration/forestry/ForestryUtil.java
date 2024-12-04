package gregtech.integration.forestry;

import gregtech.api.util.Mods;
import gregtech.integration.IntegrationModule;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.bees.GTDropType;

import net.minecraft.item.ItemStack;

import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFlowers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForestryUtil {

    @Nullable
    public static IAlleleBeeEffect getEffect(@NotNull Mods mod, @NotNull String name) {
        String s = switch (mod) {
            case ExtraBees -> "extrabees.effect." + name;
            case MagicBees -> "magicbees.effect" + name;
            case GregTech -> "gregtech.effect." + name;
            default -> "forestry.effect" + name;
        };
        return (IAlleleBeeEffect) AlleleManager.alleleRegistry.getAllele(s);
    }

    @Nullable
    public static IAlleleFlowers getFlowers(@NotNull Mods mod, @NotNull String name) {
        String s = switch (mod) {
            case ExtraBees -> "extrabees.flower." + name;
            case MagicBees -> "magicbees.flower" + name;
            case GregTech -> "gregtech.flower." + name;
            default -> "forestry.flowers" + name;
        };
        return (IAlleleFlowers) AlleleManager.alleleRegistry.getAllele(s);
    }

    @Nullable
    public static IAlleleBeeSpecies getSpecies(@NotNull Mods mod, @NotNull String name) {
        String s = switch (mod) {
            case ExtraBees -> "extrabees.species." + name;
            case MagicBees -> "magicbees.species" + name;
            case GregTech -> "gregtech.species." + name;
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
        if (!Mods.ForestryApiculture.isModLoaded()) {
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
        if (!Mods.ForestryApiculture.isModLoaded()) {
            IntegrationModule.logger.error("Tried to get GregTech Drop stack, but Apiculture module is not enabled!");
            return ItemStack.EMPTY;
        }
        return new ItemStack(ForestryModule.DROPS, amount, type.ordinal());
    }
}
