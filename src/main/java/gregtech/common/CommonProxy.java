package gregtech.common;

import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.enchants.EnchantmentEnderDamage;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import gregtech.api.recipes.recipeproperties.FusionEUToStartProperty;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.common.blocks.*;
import gregtech.common.blocks.wood.BlockGregLeaves;
import gregtech.common.blocks.wood.BlockGregLog;
import gregtech.common.blocks.wood.BlockGregSapling;
import gregtech.common.items.MetaItems;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemBlockItemPipe;
import gregtech.loaders.MaterialInfoLoader;
import gregtech.loaders.OreDictionaryLoader;
import gregtech.loaders.oreprocessing.DecompositionRecipeHandler;
import gregtech.loaders.oreprocessing.RecipeHandlerList;
import gregtech.loaders.oreprocessing.ToolRecipeHandler;
import gregtech.loaders.recipe.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

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
        registry.register(FUSION_COIL);
        registry.register(WARNING_SIGN);
        registry.register(GRANITE);
        registry.register(MINERAL);
        registry.register(CONCRETE);
        registry.register(LOG);
        registry.register(LEAVES);
        registry.register(SAPLING);
        registry.register(SURFACE_ROCK);

        COMPRESSED.values().stream().distinct().forEach(registry::register);
        FRAMES.values().stream().distinct().forEach(registry::register);
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
        ToolRecipeHandler.initializeMetaItems();

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
        registry.register(createItemBlock(FUSION_COIL, VariantItemBlock::new));
        registry.register(createItemBlock(WARNING_SIGN, VariantItemBlock::new));
        registry.register(createItemBlock(GRANITE, StoneItemBlock::new));
        registry.register(createItemBlock(MINERAL, StoneItemBlock::new));
        registry.register(createItemBlock(CONCRETE, StoneItemBlock::new));
        registry.register(createMultiTexItemBlock(LOG, state -> state.getValue(BlockGregLog.VARIANT).getName()));
        registry.register(createMultiTexItemBlock(LEAVES, state -> state.getValue(BlockGregLeaves.VARIANT).getName()));
        registry.register(createMultiTexItemBlock(SAPLING, state -> state.getValue(BlockGregSapling.VARIANT).getName()));

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
    }

    //this is called with normal priority, so most mods working with
    //ore dictionary and recipes will get recipes accessible in time
    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // registers coiltypes for the BlastTemperatureProperty used in Blast Furnace Recipes
        for (BlockWireCoil.CoilType values : BlockWireCoil.CoilType.values()) {
            BlastTemperatureProperty.registerCoilType(values.getCoilTemperature(), values.getMaterial(),
                    "tile.wire_coil." + values.getName() + ".name");
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

        MetaItems.registerRecipes();
        MachineRecipeLoader.init();
        CraftingRecipeLoader.init();
        MetaTileEntityLoader.init();
        RecipeHandlerList.register();
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
        DecompositionRecipeHandler.runRecipeGeneration();
        RecyclingRecipes.init();
        WoodMachineRecipes.init();
        
        if (GTValues.isModLoaded(GTValues.MODID_CT)){
            MetaItemBracketHandler.rebuildComponentRegistry();
        }
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        EnchantmentEnderDamage.INSTANCE.register(event);
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
        if (block == MetaBlocks.LOG) {
            event.setBurnTime(300);
        } else if (block == MetaBlocks.SAPLING) {
            event.setBurnTime(100);
        }
        //handle material blocks burn value
        if (stack.getItem() instanceof CompressedItemBlock) {
            CompressedItemBlock itemBlock = (CompressedItemBlock) stack.getItem();
            Material material = itemBlock.getBlockState(stack).getValue(itemBlock.compressedBlock.variantProperty);
            if (material instanceof DustMaterial &&
                ((DustMaterial) material).burnTime > 0) {
                //compute burn value for block prefix, taking amount of material in block into account
                double materialUnitsInBlock = OrePrefix.block.getMaterialAmount(material) / (GTValues.M * 1.0);
                event.setBurnTime((int) (materialUnitsInBlock * ((DustMaterial) material).burnTime));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static <T extends Block> ItemBlock createMultiTexItemBlock(T block, Function<IBlockState, String> nameProducer) {
        ItemBlock itemBlock = new ItemMultiTexture(block, block, stack -> {
            IBlockState blockState = block.getStateFromMeta(stack.getMetadata());
            return nameProducer.apply(blockState);
        });
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

    public void onPreLoad() {
    }

    public void onLoad() {
    }

    public void onPostLoad() {
        WoodMachineRecipes.postInit();
    }
}
