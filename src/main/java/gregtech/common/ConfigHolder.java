package gregtech.common;

import gregtech.api.GTValues;
import net.minecraftforge.common.config.Config;

@Config(modid = GTValues.MODID)
public class ConfigHolder {

    @Config.Comment("Category of options added by GregTechCE Unofficial")
    @Config.Name("Unofficial Options")
    public static UnofficialOptions U = new UnofficialOptions();

    @Config.Comment("Whether to enable more verbose logging. Default: false")
    public static boolean debug = false;

    @Config.Comment("Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically. Default: true")
    public static boolean increaseDungeonLoot = true;

    @Config.Comment("Whether to hide facades of all blocks in JEI and creative search menu. Default: true")
    @Config.RequiresMcRestart
    public static boolean hideFacadesInJEI = true;

    @Config.Comment("Whether to hide filled cells in JEI and creative search menu. Default: true")
    @Config.RequiresMcRestart
    public static boolean hideFilledCellsInJEI = true;

    @Config.Comment("Whether to hide filled tanks in JEI and creative search menu. Default: true")
    @Config.RequiresMcRestart
    public static boolean hideFilledTanksInJEI = true;

    @Config.Comment("Specifies min amount of veins in section. Default: 0")
    public static int minVeinsInSection = 0;

    @Config.Comment("Specifies additional random amount of veins in section. Default: 2")
    public static int additionalVeinsInSection = 2;

    @Config.Comment("Whether veins should be generated in center of chunk. Default: false")
    public static boolean generateVeinsInCenterOfChunk = false;

    @Config.Comment("Whether to disable vanilla ores generation in world. Default: false")
    public static boolean disableVanillaOres = false;

    @Config.Comment("Whether to disable rubber tree world generation. Default: false")
    @Config.RequiresMcRestart
    public static boolean disableRubberTreeGeneration = false;

    @Config.Comment("Whether machines should explode when overloaded with power. Default: true")
    public static boolean doExplosions = true;

    @Config.Comment("Energy use multiplier for electric items. Default: 100")
    public static int energyUsageMultiplier = 100;

    @Config.Comment("Chance of generating abandoned base in chunk = 1 / THIS_VALUE. 0 disables abandoned base generation. Default: 1000")
    public static int abandonedBaseRarity = 1000;

    @Config.RangeInt(min = 0, max = 100)
    @Config.Comment("Chance with which flint and steel will create fire. Default: 50")
    public static int flintChanceToCreateFire = 50;

    @Config.Comment("Recipes for machine hulls use more materials. Default: false")
    @Config.RequiresMcRestart
    public static boolean harderMachineHulls = false;

    @Config.Comment("If true, insufficient energy supply will reset recipe progress to zero. If false, progress will decrease to zero with 2x speed. Default: false")
    @Config.RequiresWorldRestart
    public static boolean insufficientEnergySupplyWipesRecipeProgress = false;

    @Config.Comment("Whether to use modPriorities setting in config for prioritizing ore dictionary item registrations. " +
        "By default, GTCE will sort ore dictionary registrations alphabetically comparing their owner ModIDs. Default: false")
    @Config.RequiresMcRestart
    public static boolean useCustomModPriorities = false;

    @Config.Comment("Specifies priorities of mods in ore dictionary item registration. First ModID has highest priority, last - lowest. " +
        "Unspecified ModIDs follow standard sorting, but always have lower priority than last specified ModID." +
        "\nFor this to work \"useCustomModPriorities\" has to be set to true.")
    @Config.RequiresMcRestart
    public static String[] modPriorities = new String[0];

    @Config.Comment("Setting this to true makes GTCE ignore error and invalid recipes that would otherwise cause crash. Default: true")
    @Config.RequiresMcRestart
    public static boolean ignoreErrorOrInvalidRecipes = true;

    @Config.Comment("Setting this to false causes GTCE to not register additional methane recipes for foods in the centrifuge. Default: true")
    @Config.RequiresMcRestart
    public static boolean addFoodMethaneRecipes = true;

    @Config.Comment("Category that contains configs for changing vanilla recipes")
    @Config.Name("Vanilla Recipe Options")
    @Config.RequiresMcRestart
    public static VanillaRecipes vanillaRecipes = new VanillaRecipes();

    @Config.Comment("Category that contains configs for the NanoSaber")
    @Config.Name("Nano Saber Options")
    public static NanoSaberConfiguration nanoSaberConfiguration = new NanoSaberConfiguration();

    @Config.Comment("Sets the bonus EU output of Steam Turbines. Default: 6144")
    @Config.RequiresMcRestart
    public static int steamTurbineBonusOutput = 6144;

    @Config.Comment("Sets the bonus EU output of Plasma Turbines. Default: 6144")
    @Config.RequiresMcRestart
    public static int plasmaTurbineBonusOutput = 6144;

    @Config.Comment("Sets the bonus EU output of Gas Turbines. Default 6144")
    @Config.RequiresMcRestart
    public static int gasTurbineBonusOutput = 6144;

    @Config.Comment("If true, powered zero loss wires will damage the player. Default: false")
    public static boolean doLosslessWiresDamage = false;

    @Config.Comment("If true, lossless cables will have lossy wires. Default: false")
    @Config.RequiresMcRestart
    public static boolean doLosslessWiresMakeLossyCables = false;

    @Config.Comment("Array of blacklisted dimension IDs in which Air Collector does not work. Default: none")
    public static int[] airCollectorDimensionBlacklist = new int[]{};

    public static class VanillaRecipes {

        @Config.Comment("Whether to nerf the paper crafting recipe. Default: true")
        public boolean nerfPaperCrafting = true;

        @Config.Comment("Whether to make flint and steel recipe require a steel nugget instead of an iron ingot. Default: true.")
        public boolean flintAndSteelRequireSteel = true;

        @Config.Comment("Whether to nerf wood crafting to 2 planks from 1 log. Default: false")
        public boolean nerfWoodCrafting = false;

        @Config.Comment("Whether to nerf wood crafting to 2 sticks from 2 planks. Default: false")
        public boolean nerfStickCrafting = false;

        @Config.Comment("Whether to make the iron bucket recipe harder by requiring a hammer and plates. Default: true")
        public boolean bucketRequirePlatesAndHammer = true;

        @Config.Comment("Recipes for items like iron doors, trapdoors, pressure plates, cauldrons, hoppers, and iron bars require iron plates and a hammer. Default: true")
        public boolean ironConsumingCraftingRecipesRequirePlates = true;

        @Config.Comment("Whether crafting a bowl requires a knife instead of only planks. Default: true")
        public boolean bowlRequireKnife = true;
    }

    public static class NanoSaberConfiguration {

        @Config.RangeDouble(min = 0, max = 100)
        @Config.Comment("The additional damage added when the NanoSaber is powered. Default: 20.0")
        @Config.RequiresMcRestart
        public double nanoSaberDamageBoost = 20;

        @Config.RangeDouble(min = 0, max = 100)
        @Config.Comment("The base damage of the NanoSaber. Default: 5.0")
        @Config.RequiresMcRestart
        public double nanoSaberBaseDamage = 5;

        @Config.Comment("Should Zombies spawn with charged, active NanoSabers on hard difficulty? Default: true")
        public boolean zombieSpawnWithSabers = true;

        @Config.RangeInt(min = 1, max = 512)
        @Config.Comment("The EU/t consumption of the NanoSaber. Default: 64")
        @Config.RequiresMcRestart
        public int energyConsumption = 64;
    }

    public static class UnofficialOptions {

        @Config.Comment("Config category for enabling higher-tier machines.")
        @Config.Name("Higher Tier Machines")
        @Config.RequiresMcRestart
        public HighTierMachines machines = new HighTierMachines();

        @Config.Comment("Config category for GT5u inspired features.")
        @Config.Name("GregTech 5 Unofficial Options")
        public GT5U GT5u = new GT5U();

        @Config.Comment("Should Drums be enabled? Default: true")
        @Config.RequiresMcRestart
        public boolean registerDrums = true;

        @Config.Comment("Should Crates be enabled? Default: true")
        @Config.RequiresMcRestart
        public boolean registerCrates = true;

        public static class GT5U {

            @Config.Comment("Set these to true to enable certain Batteries.")
            @Config.Name("Batteries - Enable an extra ZPM and UV Battery (this also makes the Ultimate Battery harder to make)")
            @Config.RequiresMcRestart
            public boolean enableZPMandUVBats = false;

            @Config.Name("Batteries - Replace the Ultimate Battery with a MAX Battery")
            @Config.RequiresMcRestart
            public boolean replaceUVwithMAXBat = false;

            @Config.Comment("This config requires 'B:Use custom machine tank sizes' = true to take effect. Changes the input tank size to the first value, and out tank size to the second value for nearly every single block machine. Units are millibuckets.")
            @Config.Name("Custom machine fluid tank sizes")
            @Config.RangeInt(min = 1)
            @Config.RequiresMcRestart
            public int[] customMachineTankSizes = new int[]{64000, 64000};

            @Config.Comment("This config enables the customization of nearly every single block machine's input and output fluid tank sizes.")
            @Config.Name("Use custom machine tank sizes")
            @Config.RequiresMcRestart
            public boolean useCustomMachineTankSizes = false;
        }

        public static class HighTierMachines {

            @Config.Comment("Enable all LuV-UV Machines, overrides individual values if true. Default: false")
            @Config.Name("LuV-UV Machines")
            public boolean midTierMachines = false;

            @Config.Comment("Enable all UHV-UXV Machines, overrides individual values if true. THESE WILL HAVE NO RECIPES BY DEFAULT WITHOUT GREGICALITY! Default: false")
            @Config.Name("UHV-UXV Machines")
            public boolean highTierMachines = false;

            @Config.Comment("Should higher tier Pumps be registered (IV-UV)? Separate from other configs. Default: false")
            public boolean highTierPumps = false;

            @Config.Comment("Should higher tier Air Collectors be registered (IV, LuV)? Separate from other configs. Default: false")
            public boolean highTierAirCollectors = false;

            @Config.Comment("Enable the Cluster Mill for making foils? Will not be overridden by \"higher tier\" settings. Default: false")
            public boolean enableClusterMill = false;

            @Config.Comment("Set these to true to enable LuV-UV tiers of machines. Default (all): false")
            public boolean midTierAlloySmelter = false;
            public boolean midTierArcFurnaces = false;
            public boolean midTierAssemblers = false;
            public boolean midTierAutoclaves = false;
            public boolean midTierBenders = false;
            public boolean midTierBreweries = false;
            public boolean midTierCanners = false;
            public boolean midTierCentrifuges = false;
            public boolean midTierChemicalBaths = false;
            public boolean midTierChemicalReactors = false;
            public boolean midTierCompressors = false;
            public boolean midTierCutters = false;
            public boolean midTierClusterMills = false;
            public boolean midTierDistilleries = false;
            public boolean midTierElectricFurnace = false;
            public boolean midTierElectrolyzers = false;
            public boolean midTierElectromagneticSeparators = false;
            public boolean midTierExtractors = false;
            public boolean midTierExtruders = false;
            public boolean midTierFermenters = false;
            public boolean midTierFluidCanners = false;
            public boolean midTierFluidExtractors = false;
            public boolean midTierFluidHeaters = false;
            public boolean midTierFluidSolidifiers = false;
            public boolean midTierForgeHammers = false;
            public boolean midTierFormingPresses = false;
            public boolean midTierLathes = false;
            public boolean midTierMicrowaves = false;
            public boolean midTierMixers = false;
            public boolean midTierOreWashers = false;
            public boolean midTierPackers = false;
            public boolean midTierPlasmaArcFurnaces = false;
            public boolean midTierPolarizers = false;
            public boolean midTierLaserEngravers = false;
            public boolean midTierSifters = false;
            public boolean midTierThermalCentrifuges = false;
            public boolean midTierMacerators = false;
            public boolean midTierUnpackers = false;
            public boolean midTierWiremills = false;

            @Config.Comment("Set these to true to enable UHV-UXV tiers of machines. THESE WILL HAVE NO RECIPES BY DEFAULT WITHOUT GREGICALITY! Default (all): false")
            public boolean highTierAlloySmelter = false;
            public boolean highTierArcFurnaces = false;
            public boolean highTierAssemblers = false;
            public boolean highTierAutoclaves = false;
            public boolean highTierBenders = false;
            public boolean highTierBreweries = false;
            public boolean highTierCanners = false;
            public boolean highTierCentrifuges = false;
            public boolean highTierChemicalBaths = false;
            public boolean highTierChemicalReactors = false;
            public boolean highTierCompressors = false;
            public boolean highTierCutters = false;
            public boolean highTierClusterMills = false;
            public boolean highTierDistilleries = false;
            public boolean highTierElectricFurnace = false;
            public boolean highTierElectrolyzers = false;
            public boolean highTierElectromagneticSeparators = false;
            public boolean highTierExtractors = false;
            public boolean highTierExtruders = false;
            public boolean highTierFermenters = false;
            public boolean highTierFluidCanners = false;
            public boolean highTierFluidExtractors = false;
            public boolean highTierFluidHeaters = false;
            public boolean highTierFluidSolidifiers = false;
            public boolean highTierForgeHammers = false;
            public boolean highTierFormingPresses = false;
            public boolean highTierLathes = false;
            public boolean highTierMicrowaves = false;
            public boolean highTierMixers = false;
            public boolean highTierOreWashers = false;
            public boolean highTierPackers = false;
            public boolean highTierPlasmaArcFurnaces = false;
            public boolean highTierPolarizers = false;
            public boolean highTierLaserEngravers = false;
            public boolean highTierSifters = false;
            public boolean highTierThermalCentrifuges = false;
            public boolean highTierMacerators = false;
            public boolean highTierUnpackers = false;
            public boolean highTierWiremills = false;
        }
    }
}
