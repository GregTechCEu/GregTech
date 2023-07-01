package gregtech.integration.forestry;

import forestry.api.core.ForestryAPI;
import forestry.core.items.IColoredItem;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.common.items.ToolItems;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.forestry.bees.GTAlleleBeeSpecies;
import gregtech.integration.forestry.bees.GTBeeDefinition;
import gregtech.integration.forestry.bees.GTCombItem;
import gregtech.integration.forestry.bees.GTDropItem;
import gregtech.integration.forestry.recipes.CombRecipes;
import gregtech.integration.forestry.recipes.ElectrodeRecipes;
import gregtech.integration.forestry.recipes.FrameRecipes;
import gregtech.integration.forestry.frames.GTFrameType;
import gregtech.integration.forestry.frames.GTItemFrame;
import gregtech.integration.forestry.tools.ScoopBehavior;
import gregtech.integration.forestry.recipes.ToolRecipes;
import gregtech.modules.GregTechModules;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_FR,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_FR,
        name = "GregTech Forestry Integration",
        descriptionKey = "gregtech.modules.fr_integration.description"
)
public class ForestryModule extends IntegrationSubmodule {

    public static GTItemFrame frameAccelerated;
    public static GTItemFrame frameMutagenic;
    public static GTItemFrame frameWorking;
    public static GTItemFrame frameDecaying;
    public static GTItemFrame frameSlowing;
    public static GTItemFrame frameStabilizing;
    public static GTItemFrame frameArborist;

    public static MetaItem<?> forestryMetaItem;

    public static MetaItem<?>.MetaValueItem electrodeApatite;
    public static MetaItem<?>.MetaValueItem electrodeBlaze;
    public static MetaItem<?>.MetaValueItem electrodeBronze;
    public static MetaItem<?>.MetaValueItem electrodeCopper;
    public static MetaItem<?>.MetaValueItem electrodeDiamond;
    public static MetaItem<?>.MetaValueItem electrodeEmerald;
    public static MetaItem<?>.MetaValueItem electrodeEnder;
    public static MetaItem<?>.MetaValueItem electrodeGold;
    public static MetaItem<?>.MetaValueItem electrodeIron;
    public static MetaItem<?>.MetaValueItem electrodeLapis;
    public static MetaItem<?>.MetaValueItem electrodeObsidian;
    public static MetaItem<?>.MetaValueItem electrodeOrchid;
    public static MetaItem<?>.MetaValueItem electrodeRubber;
    public static MetaItem<?>.MetaValueItem electrodeTin;

    public static IGTTool SCOOP;

    public static GTDropItem drops;
    public static GTCombItem combs;

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ForestryModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        forestryMetaItem = new StandardMetaItem();
        forestryMetaItem.setRegistryName("forestry_meta_item");

        // GT Frames
        if (ForestryConfig.enableGTFrames) {
            if (ForestryUtil.apicultureEnabled()) {
                frameAccelerated = new GTItemFrame(GTFrameType.ACCELERATED);
                frameMutagenic = new GTItemFrame(GTFrameType.MUTAGENIC);
                frameWorking = new GTItemFrame(GTFrameType.WORKING);
                frameDecaying = new GTItemFrame(GTFrameType.DECAYING);
                frameSlowing = new GTItemFrame(GTFrameType.SLOWING);
                frameStabilizing = new GTItemFrame(GTFrameType.STABILIZING);
                frameArborist = new GTItemFrame(GTFrameType.ARBORIST);
            } else {
                getLogger().warn("GregTech Frames are enabled, but Forestry Apiculture module is disabled. Skipping...");
            }
        }

        // GT Scoop
        if (ForestryConfig.enableGTScoop) {
            SCOOP = ToolItems.register(ItemGTTool.Builder.of(GTValues.MODID, "scoop")
                    .toolStats(b -> b
                            .cannotAttack().attackSpeed(-2.4F)
                            .behaviors(ScoopBehavior.INSTANCE))
                    .toolClasses("scoop")
                    .oreDict("toolScoop"));
        }

        // GT Bees
        if (ForestryConfig.enableGTBees) {
            if (ForestryUtil.apicultureEnabled()) {
                drops = new GTDropItem();
                combs = new GTCombItem();
            } else {
                getLogger().warn("GregTech Bees are enabled, but Forestry Apiculture module is disabled. Skipping...");
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Yes, this stuff has to be done in init. Because Forestry refuses to move their recipes to the event,
        // causing removals to need to be done in init instead of registry event.
        // See https://github.com/ForestryMC/ForestryMC/issues/2599
        if (ForestryConfig.enableGTElectronTubes) {
            ElectrodeRecipes.onInit();
        }

        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTBees) {
                GTAlleleBeeSpecies.setupAlleles();
                GTBeeDefinition.initBees();
            }
        }

        if (event.getSide() == Side.CLIENT) {
            if (ForestryUtil.apicultureEnabled()) {
                if (ForestryConfig.enableGTBees) {
                    Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
                        if (stack.getItem() instanceof IColoredItem coloredItem) {
                            return coloredItem.getColorFromItemstack(stack, tintIndex);
                        }
                        return 0xFFFFFF;
                    }, drops, combs);
                }
            }
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (ForestryUtil.apicultureEnabled()) {
            getLogger().info("Copying Forestry Centrifuge recipes to GT Centrifuge");
            CombRecipes.initForestryCombs();
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        // GT Frames
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                registry.register(frameAccelerated);
                registry.register(frameMutagenic);
                registry.register(frameWorking);
                registry.register(frameDecaying);
                registry.register(frameSlowing);
                registry.register(frameStabilizing);
                registry.register(frameArborist);
            }
        }

        // GT Electrodes
        if (ForestryConfig.enableGTElectronTubes) {
            electrodeApatite = forestryMetaItem.addItem(1, "electrode.apatite");
            electrodeBlaze = forestryMetaItem.addItem(2, "electrode.blaze");
            electrodeBronze = forestryMetaItem.addItem(3, "electrode.bronze");
            electrodeCopper = forestryMetaItem.addItem(4, "electrode.copper");
            electrodeDiamond = forestryMetaItem.addItem(5, "electrode.diamond");
            electrodeEmerald = forestryMetaItem.addItem(6, "electrode.emerald");
            electrodeEnder = forestryMetaItem.addItem(7, "electrode.ender");
            electrodeGold = forestryMetaItem.addItem(8, "electrode.gold");
            electrodeLapis = forestryMetaItem.addItem(9, "electrode.lapis");
            electrodeObsidian = forestryMetaItem.addItem(10, "electrode.obsidian");
            electrodeTin = forestryMetaItem.addItem(11, "electrode.tin");

            if (Loader.isModLoaded(GTValues.MODID_IC2) || Loader.isModLoaded(GTValues.MODID_BINNIE)) {
                electrodeIron = forestryMetaItem.addItem(12, "electrode.iron");
            }
            if (Loader.isModLoaded(GTValues.MODID_XU2)) {
                electrodeOrchid = forestryMetaItem.addItem(13, "electrode.orchid");
            }
            if (Loader.isModLoaded(GTValues.MODID_IC2) || Loader.isModLoaded(GTValues.MODID_TR) || Loader.isModLoaded(GTValues.MODID_BINNIE)) {
                electrodeRubber = forestryMetaItem.addItem(14, "electrode.rubber");
            }
        }

        // GT Drops
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTBees) {
                registry.register(drops);
                registry.register(combs);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                frameAccelerated.registerModel(frameAccelerated, ForestryAPI.modelManager);
                frameMutagenic.registerModel(frameMutagenic, ForestryAPI.modelManager);
                frameWorking.registerModel(frameWorking, ForestryAPI.modelManager);
                frameDecaying.registerModel(frameDecaying, ForestryAPI.modelManager);
                frameSlowing.registerModel(frameSlowing, ForestryAPI.modelManager);
                frameStabilizing.registerModel(frameStabilizing, ForestryAPI.modelManager);
                frameArborist.registerModel(frameArborist, ForestryAPI.modelManager);
            }
            if (ForestryConfig.enableGTBees) {
                drops.registerModel(drops, ForestryAPI.modelManager);
                combs.registerModel(combs, ForestryAPI.modelManager);
            }
        }
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                FrameRecipes.init();
            }
        }

        // GT Electrodes
        if (ForestryConfig.enableGTElectronTubes) {
            ElectrodeRecipes.onRecipeEvent();
        }

        // GT Scoop
        if (ForestryConfig.enableGTScoop) {
            ToolRecipes.registerHandlers();
        }

        // GT Combs
        if (ForestryConfig.enableGTBees) {
            CombRecipes.initGTCombs();
        }
    }

    @SubscribeEvent
    public static void registerMaterials(MaterialEvent event) {
        if (ForestryUtil.apicultureEnabled()) {
            if (ForestryConfig.enableGTFrames) {
                Materials.TreatedWood.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Uranium235.addFlags(MaterialFlags.GENERATE_LONG_ROD);
                Materials.Plutonium241.addFlags(MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_FOIL);
                Materials.BlueSteel.addFlags(MaterialFlags.GENERATE_LONG_ROD);
            }
        }
    }
}
