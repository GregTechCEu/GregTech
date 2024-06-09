package gtrmcore.common;

import net.minecraftforge.common.config.Config;

import gtrmcore.api.GTRMValues;

@Config(modid = GTRMValues.MODID)
public class GTRMConfigHolder {

    @Config.Name("Gregtech Override")
    @Config.RequiresMcRestart
    public static final GregtechOverride ceuOverride = new GregtechOverride();

    @Config.Name("AE2 Integration")
    @Config.RequiresMcRestart
    public static final AE2Integration ae2Integration = new AE2Integration();

    @Config.Name("EnderIO Integration")
    @Config.RequiresMcRestart
    public static final EIOIntegration eioIntegration = new EIOIntegration();

    @Config.Name("DE/DA Integration")
    @Config.RequiresMcRestart
    public static final DEDAIntegration dedaIntegration = new DEDAIntegration();

    @Config.Name("Chisel Integration")
    @Config.RequiresMcRestart
    public static final ChiselIntegration chiselIntegration = new ChiselIntegration();

    public static class GregtechOverride {

        @Config.Comment({ "Making Planks even more difficult.",
                "CEu's nerfWoodCrafting to true to reflect.", "Default: false" })
        public boolean moreNerfPlankCrafting = false;

        @Config.Comment({ "Making Sticks even more difficult.",
                "CEu's harderRods to true to reflect.", "Default: false" })
        public boolean moreNerfStickCrafting = false;

        @Config.Comment({ "Change to a recipe using Assembly Line.",
                "CEu's enableHighTierSolars to true to reflect.", "Default: false" })
        public boolean hardSolarPanel = false;

        @Config.Comment({ "Raising Terracotta Grinding from ULV to MV.", "Default: false" })
        public boolean nerfTerracottaCrafting = false;

        @Config.Comment({ "Recipe type Options: false (2x2 crafting), true (3x3 crafting).", "Default: false" })
        public boolean hardPrimitiveParts = false;
    }

    public static class AE2Integration {

        @Config.Comment({ "The voltage at which AE can be started.",
                "The material is also adjusted to each voltage.", "Default: 3 (HV)" })
        @Config.RangeInt(min = 1, max = 10)
        public int voltageTier = 3;

        @Config.Comment({ "Change AE swords, axes, etc. to GT recipe standards.",
                "CEu's hardToolArmorRecipes to true to reflect.", "Default: false" })
        public boolean hardToolRecipes = false;

        @Config.Comment({ "Integrate Printed Silicon and various Circuit creation molds.", "Default: false" })
        public boolean moveSteelShape = false;
    }

    public static class EIOIntegration {

        @Config.Comment({ "The voltage at which EIO can be started.",
                "The material is also adjusted to each voltage.", "Default: 3 (HV)" })
        @Config.RangeInt(min = 1, max = 8)
        public int voltageTier = 3;

        @Config.Comment({ "Change EIO swords, axes, armor, etc. to GT recipe standards.",
                "CEu's hardToolArmorRecipes to true to reflect.", "Default: false" })
        public boolean hardToolArmorRecipes = false;

        @Config.Comment({ "Add Shapeless Recipe in CoreMod Machines and EIO Machines.",
                "This change adds a recipe for equivalent exchange of HV machines and EIO machines", "Default: false" })
        public boolean addShapelessRecipeMachines = false;
    }

    public static class DEDAIntegration {

        @Config.Comment({ "The voltage at which DE/DA can be started.",
                "The material is also adjusted to each voltage.", "Default: 6 (LuV)" })
        @Config.RangeInt(min = 3, max = 6)
        public int voltageTier = 6;
    }

    public static class ChiselIntegration {

        @Config.Comment({ "Change Chisel recipes to GT recipe standards.",
                "CEu's hardToolArmorRecipes to true to reflect.", "Default: false" })
        public boolean hardToolRecipes = false;

        @Config.Comment({ "Change LED for Project:RED recipes to GT recipe standards.", "Default: false" })
        public boolean hardLedRecipes = false;
    }
}
