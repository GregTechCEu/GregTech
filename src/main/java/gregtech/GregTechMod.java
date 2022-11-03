package gregtech;

import codechicken.lib.CodeChickenLib;
import crafttweaker.CraftTweakerAPI;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.SimpleCapabilityManager;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.fluids.MetaFluids;
import gregtech.api.gui.UIFactory;
import gregtech.api.items.gui.PlayerInventoryUIFactory;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.net.NetworkHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.sound.GTSounds;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.CapesRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.NBTUtil;
import gregtech.api.util.VirtualTankRegistry;
import gregtech.api.util.input.KeyBind;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinSaveData;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.CommonProxy;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaEntities;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.command.GregTechCommand;
import gregtech.common.covers.CoverBehaviors;
import gregtech.common.covers.filter.FilterTypeRegistry;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.worldgen.LootTableHelper;
import gregtech.integration.GroovyScriptCompat;
import gregtech.integration.theoneprobe.TheOneProbeCompatibility;
import gregtech.loaders.dungeon.DungeonLootLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

import static gregtech.api.GregTechAPI.*;

@Mod(modid = GTValues.MODID,
        name = "GregTech",
        acceptedMinecraftVersions = "[1.12,1.13)",
        dependencies = "required:forge@[14.23.5.2847,);" + CodeChickenLib.MOD_VERSION_DEP + "after:forestry;after:jei@[4.15.0,);after:crafttweaker@[4.1.20,);")
public class GregTechMod {

    static {
        FluidRegistry.enableUniversalBucket();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            BloomEffectUtil.init();
        }
    }

    @Mod.Instance(GTValues.MODID)
    public static GregTechMod instance;

    @SidedProxy(modId = GTValues.MODID, clientSide = "gregtech.client.ClientProxy", serverSide = "gregtech.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();

        /* init GroovyScript compat */
        if (Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT)) {
            GroovyScriptCompat.init();
        }

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

        /* Start Material Registration */

        // First, register CEu Materials
        MATERIAL_REGISTRY.unfreeze();
        GTLog.logger.info("Registering GTCEu Materials");
        Materials.register();

        // Then, register addon Materials
        GTLog.logger.info("Registering addon Materials");
        MinecraftForge.EVENT_BUS.post(new MaterialEvent());

        // Then, run CraftTweaker Material registration scripts
        if (Loader.isModLoaded(GTValues.MODID_CT)) {
            GTLog.logger.info("Running early CraftTweaker initialization scripts...");
            runEarlyCraftTweakerScripts();
            MinecraftForge.EVENT_BUS.register(this);
        }

        // Fire Post-Material event, intended for when Materials need to be iterated over in-full before freezing
        // Block entirely new Materials from being added in the Post event
        MATERIAL_REGISTRY.closeRegistry();
        MinecraftForge.EVENT_BUS.post(new PostMaterialEvent());

        // Freeze Material Registry before processing Items, Blocks, and Fluids
        MATERIAL_REGISTRY.freeze();
        /* End Material Registration */

        OreDictUnifier.init();
        NBTUtil.registerSerializers();

        MetaBlocks.init();
        MetaItems.init();
        MetaFluids.init();

        GTSounds.registerSounds();

        /* Start MetaTileEntity Registration */
        MTE_REGISTRY.unfreeze();
        GTLog.logger.info("Registering GTCEu Meta Tile Entities");
        MetaTileEntities.init();
        /* End CEu MetaTileEntity Registration */
        /* Addons not done via an Event due to how much must be initialized for MTEs to register */

        MetaEntities.init();

        /* Start Heating Coil Registration */
        for (BlockWireCoil.CoilType type : BlockWireCoil.CoilType.values()) {
            HEATING_COILS.put(MetaBlocks.WIRE_COIL.getState(type), type);
        }
        /* End Heating Coil Registration */

        proxy.onPreLoad();
        KeyBind.init();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MTE_REGISTRY.freeze(); // freeze once addon preInit is finished
        proxy.onLoad();
        if (RecipeMap.isFoundInvalidRecipe()) {
            GTLog.logger.fatal("Seems like invalid recipe was found.");
            //crash if config setting is set to false, or we are in deobfuscated environment
            if (!ConfigHolder.misc.ignoreErrorOrInvalidRecipes || !FMLForgePlugin.RUNTIME_DEOBF) {
                GTLog.logger.fatal("Loading cannot continue. Either fix or report invalid recipes, or enable ignoreErrorOrInvalidRecipes in the config as a temporary solution");
                throw new LoaderException("Found at least one invalid recipe. Please read the log above for more details.");
            } else {
                GTLog.logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                GTLog.logger.fatal("Ignoring invalid recipes and continuing loading");
                GTLog.logger.fatal("Some things may lack recipes or have invalid ones, proceed at your own risk");
                GTLog.logger.fatal("Report to GTCEu GitHub to get more help and fix the problem");
                GTLog.logger.fatal("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }

        if (Loader.isModLoaded(GTValues.MODID_TOP)) {
            GTLog.logger.info("TheOneProbe found. Enabling integration...");
            TheOneProbeCompatibility.registerCompatibility();
        }

        WorldGenRegistry.INSTANCE.initializeRegistry();

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
        BedrockFluidVeinHandler.recalculateChances(true);
        // registers coil types for the BlastTemperatureProperty used in Blast Furnace Recipes
        // runs AFTER craftTweaker
        for (Map.Entry<IBlockState, IHeatingCoilBlockStats> entry : GregTechAPI.HEATING_COILS.entrySet()) {
            IHeatingCoilBlockStats value = entry.getValue();
            if (value != null) {
                String name = entry.getKey().getBlock().getTranslationKey();
                if (!name.endsWith(".name")) name = String.format("%s.name", name);
                TemperatureProperty.registerCoilType(value.getCoilTemperature(), value.getMaterial(), name);
            }
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.onLoadComplete(event);
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new GregTechCommand());
        CapesRegistry.load();
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
            if (!world.isRemote) {
                BedrockFluidVeinSaveData saveData = (BedrockFluidVeinSaveData) world.loadData(BedrockFluidVeinSaveData.class, BedrockFluidVeinSaveData.dataName);
                if (saveData == null) {
                    saveData = new BedrockFluidVeinSaveData(BedrockFluidVeinSaveData.dataName);
                    world.setData(BedrockFluidVeinSaveData.dataName, saveData);
                }
                BedrockFluidVeinSaveData.setInstance(saveData);
            }
        }
    }

    @Mod.EventHandler
    public static void onServerStopped(FMLServerStoppedEvent event) {
        VirtualTankRegistry.clearMaps();
        CapesRegistry.clearMaps();
    }

}
