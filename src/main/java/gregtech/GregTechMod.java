package gregtech;

import codechicken.lib.CodeChickenLib;
import crafttweaker.CraftTweakerAPI;
import gregtech.api.GTValues;
import gregtech.api.capability.SimpleCapabilityManager;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.items.gui.PlayerInventoryUIFactory;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.model.ResourcePackHook;
import gregtech.api.net.NetworkHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.Elements;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.util.AnnotatedMaterialHandlerLoader;
import gregtech.api.util.GTLog;
import gregtech.api.util.NBTUtil;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.common.CommonProxy;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaEntities;
import gregtech.common.MetaFluids;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.modelfactories.BlockCompressedFactory;
import gregtech.common.blocks.modelfactories.BlockFrameFactory;
import gregtech.common.blocks.modelfactories.BlockOreFactory;
import gregtech.common.command.GregTechCommand;
import gregtech.common.covers.CoverBehaviors;
import gregtech.common.covers.filter.FilterTypeRegistry;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.worldgen.LootTableHelper;
import gregtech.common.worldgen.WorldGenAbandonedBase;
import gregtech.common.worldgen.WorldGenRubberTree;
import gregtech.integration.theoneprobe.TheOneProbeCompatibility;
import gregtech.loaders.dungeon.DungeonLootLoader;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = GTValues.MODID,
        name = "GregTech",
        acceptedMinecraftVersions = "[1.12,1.13)",
        dependencies = "required:forge@[14.23.5.2847,);" + CodeChickenLib.MOD_VERSION_DEP + "after:forestry;after:jei@[4.15.0,);after:crafttweaker;")
public class GregTechMod {

    static {
        FluidRegistry.enableUniversalBucket();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ResourcePackHook.init();
            BlockOreFactory.init();
            BlockCompressedFactory.init();
            BlockFrameFactory.init();
        }
    }

    @Mod.Instance(GTValues.MODID)
    public static GregTechMod instance;

    @SidedProxy(modId = GTValues.MODID, clientSide = "gregtech.common.ClientProxy", serverSide = "gregtech.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        GTLog.init(event.getModLog());
        NetworkHandler.init();
        Elements.register();
        MetaTileEntityUIFactory.INSTANCE.init();
        PlayerInventoryUIFactory.INSTANCE.init();
        CoverBehaviorUIFactory.INSTANCE.init();
        SimpleCapabilityManager.init();
        OreDictUnifier.init();
        NBTUtil.registerSerializers();

        //first, register primary materials and run material handlers
        Materials.register();
        AnnotatedMaterialHandlerLoader.discoverAndLoadAnnotatedMaterialHandlers(event.getAsmData());
        Material.runMaterialHandlers();

        //then, run CraftTweaker early material registration scripts
        if (GTValues.isModLoaded(GTValues.MODID_CT)) {
            GTLog.logger.info("Running early CraftTweaker initialization scripts...");
            runEarlyCraftTweakerScripts();
        }

        //freeze material registry before processing items, blocks and fluids
        Material.freezeRegistry();

        MetaBlocks.init();
        MetaItems.init();
        MetaFluids.init();
        MetaTileEntities.init();
        MetaEntities.init();

        proxy.onPreLoad();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.onLoad();
        if (RecipeMap.isFoundInvalidRecipe()) {
            GTLog.logger.fatal("Seems like invalid recipe was found.");
            //crash if config setting is set to false, or we are in deobfuscated environment
            if (!ConfigHolder.ignoreErrorOrInvalidRecipes || !FMLForgePlugin.RUNTIME_DEOBF) {
                GTLog.logger.fatal("Loading cannot continue. Either fix or report invalid recipes, or enable ignoreErrorOrInvalidRecipes in the config as a temporary solution");
                throw new LoaderException("Found at least one invalid recipe. Please read the log above for more details.");
            } else {
                GTLog.logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                GTLog.logger.fatal("Ignoring invalid recipes and continuing loading");
                GTLog.logger.fatal("Some things may lack recipes or have invalid ones, proceed at your own risk");
                GTLog.logger.fatal("Report to GTCE github to get more help and fix the problem");
                GTLog.logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }
        RecipeMap.sortMaps();

        if (GTValues.isModLoaded(GTValues.MODID_TOP)) {
            GTLog.logger.info("TheOneProbe found. Enabling integration...");
            TheOneProbeCompatibility.registerCompatibility();
        }

        WorldGenRegistry.INSTANCE.initializeRegistry();
        GameRegistry.registerWorldGenerator(new WorldGenAbandonedBase(), 20000);
        if (!ConfigHolder.disableRubberTreeGeneration) {
            GameRegistry.registerWorldGenerator(new WorldGenRubberTree(), 10000);
        }

        LootTableHelper.initialize();
        FilterTypeRegistry.init();
        CoverBehaviors.init();
        DungeonLootLoader.init();
    }

    @Method(modid = GTValues.MODID_CT)
    private void runEarlyCraftTweakerScripts() {
        CraftTweakerAPI.tweaker.loadScript(false, "gregtech");
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new GregTechCommand());
    }

}
