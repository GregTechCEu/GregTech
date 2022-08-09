package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.enchants.EnchantmentEnderDamage;
import gregtech.api.enchants.EnchantmentHardHammer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.ItemMaterialInfo;
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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

import static gregtech.common.blocks.MetaBlocks.*;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GTLog.logger.info("Registering Blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(MACHINE);

        StoneType.init();

        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {

           if (material.hasProperty(PropertyKey.ORE)) {
                createOreBlock(material);
            }

            if (material.hasProperty(PropertyKey.WIRE)) {
                for (BlockCable cable : CABLES) {
                    if (!cable.getItemPipeType(null).isCable() || !material.getProperty(PropertyKey.WIRE).isSuperconductor())
                        cable.addCableMaterial(material, material.getProperty(PropertyKey.WIRE));
                }
            }
            if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
                for (BlockFluidPipe pipe : FLUID_PIPES) {
                    if(!pipe.getItemPipeType(pipe.getItem(material)).getOrePrefix().isIgnored(material)) {
                        pipe.addPipeMaterial(material, material.getProperty(PropertyKey.FLUID_PIPE));
                    }
                }
            }
            if (material.hasProperty(PropertyKey.ITEM_PIPE)) {
                for (BlockItemPipe pipe : ITEM_PIPES) {
                    if(!pipe.getItemPipeType(pipe.getItem(material)).getOrePrefix().isIgnored(material)) {
                        pipe.addPipeMaterial(material, material.getProperty(PropertyKey.ITEM_PIPE));
                    }
                }
            }
        }
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            if(!pipe.getItemPipeType(pipe.getItem(Materials.Wood)).getOrePrefix().isIgnored(Materials.Wood) ||
                    !pipe.getItemPipeType(pipe.getItem(Materials.TreatedWood)).getOrePrefix().isIgnored(Materials.TreatedWood)) {
                pipe.addPipeMaterial(Materials.Wood, new FluidPipeProperties(340, 5, false, false, false, false));
                pipe.addPipeMaterial(Materials.TreatedWood, new FluidPipeProperties(340, 10, false, false, false, false));
            }
        }

        for (BlockCable cable : CABLES) registry.register(cable);
        for (BlockFluidPipe pipe : FLUID_PIPES) registry.register(pipe);
        for (BlockItemPipe pipe : ITEM_PIPES) registry.register(pipe);

        registry.register(HERMETIC_CASING);
        registry.register(CLEANROOM_CASING);
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
        registry.register(WARNING_SIGN_1);
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

    private static void createOreBlock(Material material) {
        StoneType[] stoneTypeBuffer = new StoneType[16];
        int generationIndex = 0;
        for (StoneType stoneType : StoneType.STONE_TYPE_REGISTRY) {
            int id = StoneType.STONE_TYPE_REGISTRY.getIDForObject(stoneType), index = id / 16;
            if (index > generationIndex) {
                createOreBlock(material, copyNotNull(stoneTypeBuffer), generationIndex);
                Arrays.fill(stoneTypeBuffer, null);
            }
            stoneTypeBuffer[id % 16] = stoneType;
            generationIndex = index;
        }
        createOreBlock(material, copyNotNull(stoneTypeBuffer), generationIndex);
    }

    private static <T> T[] copyNotNull(T[] src) {
        int nullIndex = ArrayUtils.indexOf(src, null);
        return Arrays.copyOfRange(src, 0, nullIndex == -1 ? src.length : nullIndex);
    }

    private static void createOreBlock(Material material, StoneType[] stoneTypes, int index) {
        BlockOre block = new BlockOre(material, stoneTypes);
        block.setRegistryName("ore_" + material + "_" + index);
        for (StoneType stoneType : stoneTypes) {
            GregTechAPI.oreBlockTable.computeIfAbsent(material, m -> new HashMap<>()).put(stoneType, block);
        }
        ORES.add(block);
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
        registry.register(createItemBlock(CLEANROOM_CASING, VariantItemBlock::new));
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
        registry.register(createItemBlock(WARNING_SIGN_1, VariantItemBlock::new));
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
        //Registers Fusion tiers for the FusionEUToStartProperty
        FusionEUToStartProperty.registerFusionTier(6, "(MK1)");
        FusionEUToStartProperty.registerFusionTier(7, "(MK2)");
        FusionEUToStartProperty.registerFusionTier(8, "(MK3)");

        GTLog.logger.info("Registering ore dictionary...");

        MetaItems.registerOreDict();
        MetaBlocks.registerOreDict();
        OreDictionaryLoader.init();
        MaterialInfoLoader.init();

        // post an event for addons to modify unification data before base GT registers recycling recipes
        MinecraftForge.EVENT_BUS.post(new GregTechAPI.RegisterEvent<>(null, ItemMaterialInfo.class));

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

        if (Loader.isModLoaded(GTValues.MODID_CT)) {
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
        if (block == RUBBER_LOG || block == PLANKS) {
            event.setBurnTime(300);
        } else if (block == RUBBER_SAPLING) {
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
        TerminalRegistry.init();

        if(ConfigHolder.compat.removeSmeltingForEBFMetals) {
            ModHandler.removeSmeltingEBFMetals();
        }
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        if(Loader.isModLoaded(GTValues.MODID_JEI) && event.getSide() == Side.CLIENT) {
            GTJeiPlugin.setupInputHandler();
        }
        GTRecipeInput.INSTANCES = new ObjectOpenHashSet<>();
    }

    public boolean isFancyGraphics() {
        return true;
    }
}
