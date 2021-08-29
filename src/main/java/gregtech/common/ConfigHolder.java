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

    @Config.Comment("If true, insufficient energy supply will reset recipe progress to zero. If false, progress will decrease to zero with 2x speed. Default: false")
    @Config.RequiresWorldRestart
    public static boolean insufficientEnergySupplyWipesRecipeProgress = false;

    @Config.Comment("Specifies priorities of mods in ore dictionary item registration. First ModID has highest priority, last - lowest. " +
            "Unspecified ModIDs follow standard sorting, but always have lower priority than last specified ModID.")
    @Config.RequiresMcRestart
    public static String[] modPriorities = {
            "minecraft",
            "gregtech",
            "gtadditions"
    };

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

    public static class VanillaRecipes {

        @Config.Comment("Whether to make glass related recipes harder. Default: true")
        public boolean hardGlassRecipes = true;

        @Config.Comment("Whether to nerf wood crafting to 2 planks from 1 log. Default: false")
        public boolean nerfWoodCrafting = false;

        @Config.Comment("Whether to nerf wood crafting to 2 sticks from 2 planks. Default: false")
        public boolean nerfStickCrafting = false;

        @Config.Comment("Whether to nerf the paper crafting recipe. Default: true")
        public boolean nerfPaperCrafting = true;

        @Config.Comment("Whether to make wood related recipes harder. Excludes sticks and planks. Default: false")
        public boolean hardWoodRecipes = false;

        @Config.Comment("Whether to make redstone related recipes harder. Default: false")
        public boolean hardRedstoneRecipes = false;

        @Config.Comment("Recipes for items like iron doors, trapdoors, pressure plates, cauldrons, hoppers, and iron bars require iron plates, sticks, and more. Default: true")
        public boolean hardIronRecipes = true;

        @Config.Comment("Whether to make miscellaneous recipes harder. Default: false")
        public boolean hardMiscRecipes = false;

        @Config.Comment("Whether to make flint and steel recipe require a steel of iron. Default: true.")
        public boolean flintAndSteelRequireSteel = true;

        @Config.Comment("Whether to make the iron bucket recipe harder by requiring a hammer and plates. Default: true")
        public boolean bucketRequirePlatesAndHammer = true;

        @Config.Comment("Whether to make vanilla tools and armor recipes harder. Excludes flint and steel, and buckets. Default: false")
        public boolean hardToolArmorRecipes = false;

        @Config.Comment("Whether to disable the vanilla Concrete from Powder with Water behavior, forcing the GT recipe. Default: false")
        public boolean disableConcreteInWorld = false;
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

        @Config.Comment("Config category for GT5u inspired features.")
        @Config.Name("GregTech 5 Unofficial Options")
        public GT5U GT5u = new GT5U();

        @Config.Comment("Config category for GT6 inspired features.")
        @Config.Name("GregTech 6 Options")
        public GT6 GT6 = new GT6();

        @Config.Comment("Config category for energy compatibility features")
        @Config.Name("Energy Compatibility Options")
        public EnergyCompatibility energyOptions = new EnergyCompatibility();

        @Config.Comment("Allow GregTech to add additional loot. Default: true")
        @Config.RequiresMcRestart
        public static boolean addLoot = true;

        @Config.Comment("Should recipes for EV and IV Drills be enabled, which may cause large amounts of lag when used on some low-end devices? Default: true")
        @Config.RequiresMcRestart
        public boolean registerRecipesForHighTierDrills = true;

        @Config.Comment("Should recipes for Mining Hammers be enabled? Default: true")
        @Config.RequiresMcRestart
        public boolean registerRecipesForMiningHammers = true;

        @Config.Comment("Divisor for Recipe Duration per Overclock. Default: 2.0")
        @Config.RangeDouble(min = 2.0, max = 3.0)
        public double overclockDivisor = 2.0;

        @Config.Comment("Whether to enable that Steam Multiblocks use Steel instead of Bronze. Default: false")
        @Config.RequiresMcRestart
        public boolean steelSteamMultiblocks = false;

        @Config.Comment("Steam to EU multiplier for Steam Multiblocks. 1.0 means 1 Steam -> 1 EU. 0.5 means 2 Steam -> 1 EU. Default: 0.5")
        @Config.RequiresWorldRestart
        public double multiblockSteamToEU = 0.5;

        public static class GT5U {

            @Config.Comment("This config requires 'B:Use custom machine tank sizes' = true to take effect. Changes the input tank size to the first value, and out tank size to the second value for nearly every single block machine. Units are millibuckets. Default: {64000, 64000}")
            @Config.RangeInt(min = 1)
            @Config.RequiresMcRestart
            public int[] customMachineTankSizes = new int[]{64000, 64000};

            @Config.Comment("This config enables the customization of nearly every single block machine's input and output fluid tank sizes. Default: false")
            @Config.RequiresMcRestart
            public boolean useCustomMachineTankSizes = false;

            @Config.Comment("Require Wrench to break machines? Default: false")
            public boolean requireWrenchForMachines = false;

            @Config.Comment("Change the recipe of rods to result in 1 stick and 2 small piles of dusts. Default: false")
            @Config.RequiresMcRestart
            public boolean harderRods = false;

            @Config.Comment("Whether or not to use polymers instead of rare metals for Carbon Fibers. REMOVES THE CHANCED OUTPUT! Default: false")
            @Config.RequiresMcRestart
            public boolean polymerCarbonFiber = false;

            @Config.Comment("The default color to overlay onto machines. \n16777215 (0xFFFFFF in decimal) is no coloring (default), and 13819135 (0xD2DCFF in decimal) is the classic blue from GT5. THIS IS SERVER SIDE!!!")
            @Config.RequiresMcRestart
            public int defaultPaintingColor = 0xFFFFFF;

            @Config.Comment("The default color to overlay onto cable insulation. \n7829367 (0x777777 in decimal) is no coloring (default), and 4210752 (0x404040 in decimal) is the classic black from GT5. THIS IS SERVER SIDE!!!")
            @Config.RequiresMcRestart
            public int defaultInsulationColor = 0x777777;

            @Config.Comment("Enable temperature based bonuses for the Electric Blast Furnace. Default: true")
            @Config.RequiresMcRestart
            public boolean ebfTemperatureBonuses = true;

            @Config.Comment("Enable more challenging recipes for Electric Blast Furnace Coils. Default: true")
            @Config.RequiresMcRestart
            public boolean harderHeatingCoils = true;

            @Config.Comment("Enable more challenging recipes for Energy Input and Output hatches. Default: false")
            @Config.RequiresMcRestart
            public boolean harderEnergyHatches = false;
        }

        public static class GT6 {

            @Config.Comment("Whether or not to use GT6-style pipe and cable connections, meaning they will not auto-connect " +
                    "unless placed directly onto another pipe or cable. Default: false")
            public boolean gt6StylePipesCables = false;

            @Config.Comment("Whether or not to use Plates instead of Ingots for Wrench Recipes. Default: false")
            @Config.RequiresMcRestart
            public boolean plateWrenches = false;
        }

        public static class EnergyCompatibility {

            @Config.Comment("Enable Native GTEU to Forge Energy (RF and alike) on GT Cables and Wires. Default: true")
            public boolean nativeEUToFE = true;

            @Config.Comment("GTEU to Forge Energy (RF and alike) ratio. Default: 4 FE to 1 EU")
            @Config.RangeDouble() // to ensure positive number
            public double rfRatio = 4;
        }
    }
}
