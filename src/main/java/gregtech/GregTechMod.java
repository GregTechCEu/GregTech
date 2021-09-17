package gregtech;

import codechicken.lib.CodeChickenLib;
import crafttweaker.CraftTweakerAPI;
import gregtech.api.GTValues;
import gregtech.api.capability.SimpleCapabilityManager;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.UIFactory;
import gregtech.api.items.gui.PlayerInventoryUIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.model.ResourcePackHook;
import gregtech.api.net.NetworkHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.IMaterialHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialRegistry;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.AnnotatedMaterialHandlerLoader;
import gregtech.api.util.GTLog;
import gregtech.api.util.NBTUtil;
import gregtech.api.util.input.KeyBinds;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.common.*;
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
import gregtech.loaders.recipe.component.AnnotatedComponentHandlerLoader;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static gregtech.api.GregTechRegistries.*;

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
        NetworkHandler.init();
        GTLog.init(event.getModLog());

        /* Start UI Factory Registration */
        UI_FACTORY_REGISTRY.unfreeze();
        GTLog.logger.info("Registering GTCEu UI Factories");
        MetaTileEntityUIFactory.INSTANCE.init();
        PlayerInventoryUIFactory.INSTANCE.init();
        CoverBehaviorUIFactory.INSTANCE.init();
        GTLog.logger.info("Registering addon UI Factories");
        MinecraftForge.EVENT_BUS.post(new RegisterEvent<>(UI_FACTORY_REGISTRY, UIFactory.class));
        UI_FACTORY_REGISTRY.freeze();
        /* End UI Factory Registration */

        SimpleCapabilityManager.init();

        // TODO Update this to new registration method
        /* Start Material Registration */
        //first, register primary materials and run material handlers
        MaterialRegistry.MATERIAL_REGISTRY.unfreeze();
        Materials.register();
        AnnotatedMaterialHandlerLoader.discoverAndLoadAnnotatedMaterialHandlers(event.getAsmData());
        IMaterialHandler.runMaterialHandlers();

        // Finalize GT materials (for now) so CT can access them by registry lookup
        MaterialRegistry.finalizeMaterials(false);

        //then, run CraftTweaker early material registration scripts
        if (GTValues.isModLoaded(GTValues.MODID_CT)) {
            GTLog.logger.info("Running early CraftTweaker initialization scripts...");
            runEarlyCraftTweakerScripts();
            MinecraftForge.EVENT_BUS.register(this);
        }

        //freeze material registry before processing items, blocks and fluids
        MaterialRegistry.finalizeMaterials(true);
        /* End Material Registration */

        OreDictUnifier.init();
        NBTUtil.registerSerializers();

        MetaBlocks.init();
        MetaItems.init();
        MetaFluids.init();

        /* Start MetaTileEntity Registration */
        MTE_REGISTRY.unfreeze();
        GTLog.logger.info("Registering GTCEu Meta Tile Entities");
        MetaTileEntities.init();
        GTLog.logger.info("Registering addon Meta Tile Entities");
        MinecraftForge.EVENT_BUS.post(new RegisterEvent<>(MTE_REGISTRY, MetaTileEntity.class));
        MTE_REGISTRY.freeze();
        /* End MetaTileEntity Registration */

        MetaEntities.init();

        // discover annotated crafting component handlers
        AnnotatedComponentHandlerLoader.discoverAndLoadAnnotatedComponentHandlers(event.getAsmData());

        proxy.onPreLoad();
        KeyBinds.register();
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

        /* Start Cover Definition Registration */
        COVER_REGISTRY.unfreeze();
        CoverBehaviors.init();
        MinecraftForge.EVENT_BUS.post(new RegisterEvent<>(COVER_REGISTRY, CoverDefinition.class));
        COVER_REGISTRY.freeze();
        /* End Cover Definition Registration */

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
