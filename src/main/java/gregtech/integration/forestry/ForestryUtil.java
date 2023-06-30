package gregtech.integration.forestry;

import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFlowers;
import forestry.modules.ModuleHelper;
import gregtech.api.GTValues;

public class ForestryUtil {

    public static boolean apicultureEnabled() {
        return ModuleHelper.isEnabled("apiculture");
    }

    public static IAlleleBeeEffect getEffect(String modid, String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.effect." + name;
            case GTValues.MODID_MB -> "magicbees.effect" + name;
            case GTValues.MODID -> "gregtech.effect." + name;
            default -> "forestry.effect" + name;
        };
        return (IAlleleBeeEffect) AlleleManager.alleleRegistry.getAllele(s);
    }

    public static IAlleleFlowers getFlowers(String modid, String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.flower." + name;
            case GTValues.MODID_MB -> "magicbees.flower" + name;
            case GTValues.MODID -> "gregtech.flower." + name;
            default -> "forestry.flowers" + name;
        };
        return (IAlleleFlowers) AlleleManager.alleleRegistry.getAllele(s);
    }

    public static IAlleleBeeSpecies getSpecies(String modid, String name) {
        String s = switch (modid) {
            case GTValues.MODID_EB -> "extrabees.species." + name;
            case GTValues.MODID_MB -> "magicbees.species" + name;
            case GTValues.MODID -> "gregtech.species." + name;
            default -> "forestry.species" + name;
        };
        return (IAlleleBeeSpecies) AlleleManager.alleleRegistry.getAllele(s);
    }
}
