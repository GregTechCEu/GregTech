package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.enchants.EnchantmentEnderDamage;
import gregtech.api.enchants.EnchantmentHardHammer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.CapesRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.advancement.GTTrigger;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.blocks.*;
import gregtech.common.items.MetaItems;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemBlockItemPipe;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.loaders.MaterialInfoLoader;
import gregtech.loaders.OreDictionaryLoader;
import gregtech.loaders.recipe.CraftingComponent;
import gregtech.loaders.recipe.GTRecipeManager;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import static gregtech.common.blocks.MetaBlocks.*;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GTLog.logger.info("Registering Blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(MACHINE);

        for (BlockCable cable : CABLES) registry.register(cable);
        for (BlockFluidPipe pipe : FLUID_PIPES) registry.register(pipe);
        for (BlockItemPipe pipe : ITEM_PIPES) registry.register(pipe);

        registry.register(HERMETIC_CASING);
        registry.register(FOAM);
        registry.register(REINFORCED_FOAM);
        registry.register(PETRIFIED_FOAM);
        registry.register(REINFORCED_PETRIFIED_FOAM);
        registry.register(BOILER_CASING);
        registry.register(BOILER_FIREBOX_CASING);
        registry.register(METAL_CASING);
        registry.register(TURBINE_CASING);
        registry.register(MACHINE_CASING);
        registry.register(STEAM_CASING);
        registry.register(MULTIBLOCK_CASING);
        registry.register(TRANSPARENT_CASING);
        registry.register(WIRE_COIL);
        registry.register(FUSION_CASING);
        registry.register(WARNING_SIGN);
        registry.register(ASPHALT);
        registry.register(STONE_SMOOTH);
        registry.register(STONE_COBBLE);
        registry.register(STONE_COBBLE_MOSSY);
        registry.register(STONE_POLISHED);
        registry.register(STONE_BRICKS);
        registry.register(STONE_BRICKS_CRACKED);
        registry.register(STONE_BRICKS_MOSSY);
        registry.register(STONE_CHISELED);
        registry.register(STONE_TILED);
        registry.register(STONE_TILED_SMALL);
        registry.register(STONE_BRICKS_SMALL);
        registry.register(STONE_WINDMILL_A);
        registry.register(STONE_WINDMILL_B);
        registry.register(STONE_BRICKS_SQUARE);
        registry.register(RUBBER_LOG);
        registry.register(RUBBER_LEAVES);
        registry.register(RUBBER_SAPLING);
        registry.register(PLANKS);

        COMPRESSED.values().stream().distinct().forEach(registry::register);
        FRAMES.values().stream().distinct().forEach(registry::register);
        SURFACE_ROCK.values().stream().distinct().forEach(registry::register);
        ORES.forEach(registry::register);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerBlocksLast(RegistryEvent.Register<Block> event) {
        //last chance for mods to register their potion types is here
        FLUID_BLOCKS.forEach(event.getRegistry()::register);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        GTLog.logger.info("Registering Items...");
        IForgeRegistry<Item> registry = event.getRegistry();

        for (MetaItem<?> item : MetaItems.ITEMS) {
            registry.register(item);
            item.registerSubItems();
        }
        GTRecipeManager.preLoad();

        registry.register(createItemBlock(MACHINE, MachineItemBlock::new));

        for (BlockCable cable : CABLES) registry.register(createItemBlock(cable, ItemBlockCable::new));
        for (BlockFluidPipe pipe : FLUID_PIPES) registry.register(createItemBlock(pipe, ItemBlockFluidPipe::new));
        for (BlockItemPipe pipe : ITEM_PIPES) registry.register(createItemBlock(pipe, ItemBlockItemPipe::new));

        registry.register(createItemBlock(HERMETIC_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(BOILER_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(BOILER_FIREBOX_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(METAL_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(TURBINE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(MACHINE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(STEAM_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(MULTIBLOCK_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(TRANSPARENT_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(WIRE_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(FUSION_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(WARNING_SIGN, VariantItemBlock::new));
        registry.register(createItemBlock(ASPHALT, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_SMOOTH, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_COBBLE, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_COBBLE_MOSSY, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_POLISHED, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_BRICKS, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_BRICKS_CRACKED, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_BRICKS_MOSSY, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_CHISELED, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_TILED, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_TILED_SMALL, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_BRICKS_SMALL, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_WINDMILL_A, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_WINDMILL_B, VariantItemBlock::new));
        registry.register(createItemBlock(STONE_BRICKS_SQUARE, VariantItemBlock::new));
        registry.register(createItemBlock(PLANKS, VariantItemBlock::new));
        registry.register(createItemBlock(RUBBER_LOG, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_LEAVES, ItemBlock::new));
        registry.register(createItemBlock(RUBBER_SAPLING, ItemBlock::new));

        COMPRESSED.values()
                .stream().distinct()
                .map(block -> createItemBlock(block, CompressedItemBlock::new))
                .forEach(registry::register);
        FRAMES.values()
                .stream().distinct()
                .map(block -> createItemBlock(block, FrameItemBlock::new))
                .forEach(registry::register);
        ORES.stream()
                .map(block -> createItemBlock(block, OreItemBlock::new))
                .forEach(registry::register);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void initComponents(RegistryEvent.Register<IRecipe> event) {
        CraftingComponent.initializeComponents();
        MinecraftForge.EVENT_BUS.post(new GregTechAPI.RegisterEvent<>(null, CraftingComponent.class));
    }

    //this is called with normal priority, so most mods working with
    //ore dictionary and recipes will get recipes accessible in time
    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // registers coiltypes for the BlastTemperatureProperty used in Blast Furnace Recipes
        for (BlockWireCoil.CoilType values : BlockWireCoil.CoilType.values()) {
            TemperatureProperty.registerCoilType(values.getCoilTemperature(), values.getMaterial(),
                    String.format("tile.wire_coil.%s.name", values.getName()));
        }

        //Registers Fusion tiers for the FusionEUToStartProperty
        FusionEUToStartProperty.registerFusionTier(6, "(MK1)");
        FusionEUToStartProperty.registerFusionTier(7, "(MK2)");
        FusionEUToStartProperty.registerFusionTier(8, "(MK3)");

        GTLog.logger.info("Registering ore dictionary...");

        MetaItems.registerOreDict();
        MetaBlocks.registerOreDict();
        OreDictionaryLoader.init();
        MaterialInfoLoader.init();

        GTLog.logger.info("Registering recipes...");

        GTRecipeManager.load();
    }

    //this is called almost last, to make sure all mods registered their ore dictionary
    //items and blocks for running first phase of material handlers
    //it will also clear generated materials
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void runEarlyMaterialHandlers(RegistryEvent.Register<IRecipe> event) {
        GTLog.logger.info("Running early material handlers...");
        OrePrefix.runMaterialHandlers();
    }

    //this is called last, so all mods finished registering their stuff, as example, CraftTweaker
    //if it registered some kind of ore dictionary entry, late processing will hook it and generate recipes
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipesLowest(RegistryEvent.Register<IRecipe> event) {
        GTLog.logger.info("Running late material handlers...");
        OrePrefix.runMaterialHandlers();
        GTRecipeManager.loadLatest();

        if (GTValues.isModLoaded(GTValues.MODID_CT)) {
            MetaItemBracketHandler.rebuildComponentRegistry();
        }
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        EnchantmentEnderDamage.INSTANCE.register(event);
        EnchantmentHardHammer.INSTANCE.register(event);
    }

    @SubscribeEvent
    public static void syncConfigValues(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(GTValues.MODID)) {
            ConfigManager.sync(GTValues.MODID, Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void modifyFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        Block block = Block.getBlockFromItem(stack.getItem());
        //handle sapling and log burn rates
        if (block == MetaBlocks.RUBBER_LOG) {
            event.setBurnTime(300);
        } else if (block == MetaBlocks.RUBBER_SAPLING) {
            event.setBurnTime(100);
        }
        //handle material blocks burn value
        if (stack.getItem() instanceof CompressedItemBlock) {
            CompressedItemBlock itemBlock = (CompressedItemBlock) stack.getItem();
            Material material = itemBlock.getBlockState(stack).getValue(itemBlock.compressedBlock.variantProperty);
            DustProperty property = material.getProperty(PropertyKey.DUST);
            if (property != null &&
                    property.getBurnTime() > 0) {
                //compute burn value for block prefix, taking amount of material in block into account
                double materialUnitsInBlock = OrePrefix.block.getMaterialAmount(material) / (GTValues.M * 1.0);
                event.setBurnTime((int) (materialUnitsInBlock * property.getBurnTime()));
            }
        }
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

    public void onPreLoad() {
    }

    public void onLoad() {
        Method triggerRegistry = ObfuscationReflectionHelper.findMethod(CriteriaTriggers.class, "func_192118_a", ICriterionTrigger.class, ICriterionTrigger.class);
        triggerRegistry.setAccessible(true);
        for (GTTrigger<?> trigger : GTTriggers.GT_TRIGGERS) {
            try {
                triggerRegistry.invoke(null, trigger);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                GTLog.logger.error("Failed to register Advancement trigger: {}", trigger.getId());
                GTLog.logger.error("Stacktrace:", e);
            }
        }
    }

    public void onPostLoad() {
        GTRecipeManager.postLoad();
        CapesRegistry.registerDevCapes();
        TerminalRegistry.init();
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        if(GTValues.isModLoaded(GTValues.MODID_JEI) && event.getSide() == Side.CLIENT) {
            GTJeiPlugin.setupInputHandler();
        }
    }

    public boolean isFancyGraphics() {
        return true;
    }
}
