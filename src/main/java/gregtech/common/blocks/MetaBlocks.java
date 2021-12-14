package gregtech.common.blocks;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.handler.MetaTileEntityTESR;
import gregtech.client.model.IModelSupplier;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.foam.BlockFoam;
import gregtech.common.blocks.foam.BlockPetrifiedFoam;
import gregtech.client.model.modelfactories.BakedModelHandler;
import gregtech.common.blocks.wood.BlockGregLeaves;
import gregtech.common.blocks.wood.BlockGregLog;
import gregtech.common.blocks.wood.BlockGregPlank;
import gregtech.common.blocks.wood.BlockGregSapling;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.pipelike.cable.tile.TileEntityCableTickable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FRAME;
import static gregtech.client.ClientProxy.*;

public class MetaBlocks {

    private MetaBlocks() {
    }

    public static BlockMachine MACHINE;
    public static final BlockCable[] CABLES = new BlockCable[10];
    public static final BlockFluidPipe[] FLUID_PIPES = new BlockFluidPipe[7];
    public static final BlockItemPipe[] ITEM_PIPES = new BlockItemPipe[6];

    public static BlockBoilerCasing BOILER_CASING;
    public static BlockFireboxCasing BOILER_FIREBOX_CASING;
    public static BlockMetalCasing METAL_CASING;
    public static BlockTurbineCasing TURBINE_CASING;
    public static BlockMachineCasing MACHINE_CASING;
    public static BlockSteamCasing STEAM_CASING;
    public static BlockMultiblockCasing MULTIBLOCK_CASING;
    public static BlockGlassCasing TRANSPARENT_CASING;
    public static BlockWireCoil WIRE_COIL;
    public static BlockWireCoil2 WIRE_COIL2;
    public static BlockFusionCasing FUSION_CASING;
    public static BlockWarningSign WARNING_SIGN;
    public static HermeticCasings HERMETIC_CASING;

    public static BlockGranite GRANITE;
    public static BlockMineral MINERAL;
    public static BlockConcrete CONCRETE;

    public static BlockFoam FOAM;
    public static BlockFoam REINFORCED_FOAM;
    public static BlockPetrifiedFoam PETRIFIED_FOAM;
    public static BlockPetrifiedFoam REINFORCED_PETRIFIED_FOAM;

    public static BlockGregLog LOG;
    public static BlockGregLeaves LEAVES;
    public static BlockGregSapling SAPLING;
    public static BlockGregPlank PLANKS;

    public static BlockSurfaceRock SURFACE_ROCK;

    public static final Map<Material, BlockCompressed> COMPRESSED = new HashMap<>();
    public static final Map<Material, BlockFrame> FRAMES = new HashMap<>();
    public static final Collection<BlockOre> ORES = new ReferenceArrayList<>();
    public static final Collection<BlockFluidBase> FLUID_BLOCKS = new ReferenceArrayList<>();

    public static void init() {
        GregTechAPI.MACHINE = MACHINE = new BlockMachine();
        MACHINE.setRegistryName("machine");

        for (Insulation ins : Insulation.values()) {
            CABLES[ins.ordinal()] = new BlockCable(ins);
            CABLES[ins.ordinal()].setRegistryName(ins.getName());
        }
        for (FluidPipeType type : FluidPipeType.values()) {
            FLUID_PIPES[type.ordinal()] = new BlockFluidPipe(type);
            FLUID_PIPES[type.ordinal()].setRegistryName(String.format("fluid_pipe_%s", type.name));
        }
        for (ItemPipeType type : ItemPipeType.values()) {
            ITEM_PIPES[type.ordinal()] = new BlockItemPipe(type);
            ITEM_PIPES[type.ordinal()].setRegistryName(String.format("item_pipe_%s", type.name));
        }

        BOILER_CASING = new BlockBoilerCasing();
        BOILER_CASING.setRegistryName("boiler_casing");
        BOILER_FIREBOX_CASING = new BlockFireboxCasing();
        BOILER_FIREBOX_CASING.setRegistryName("boiler_firebox_casing");
        METAL_CASING = new BlockMetalCasing();
        METAL_CASING.setRegistryName("metal_casing");
        TURBINE_CASING = new BlockTurbineCasing();
        TURBINE_CASING.setRegistryName("turbine_casing");
        MACHINE_CASING = new BlockMachineCasing();
        MACHINE_CASING.setRegistryName("machine_casing");
        STEAM_CASING = new BlockSteamCasing();
        STEAM_CASING.setRegistryName("steam_casing");
        MULTIBLOCK_CASING = new BlockMultiblockCasing();
        MULTIBLOCK_CASING.setRegistryName("multiblock_casing");
        TRANSPARENT_CASING = new BlockGlassCasing();
        TRANSPARENT_CASING.setRegistryName("transparent_casing");
        WIRE_COIL = new BlockWireCoil();
        WIRE_COIL.setRegistryName("wire_coil");
        WIRE_COIL2 = new BlockWireCoil2();
        WIRE_COIL2.setRegistryName("wire_coil2");
        FUSION_CASING = new BlockFusionCasing();
        FUSION_CASING.setRegistryName("fusion_casing");
        WARNING_SIGN = new BlockWarningSign();
        WARNING_SIGN.setRegistryName("warning_sign");
        HERMETIC_CASING = new HermeticCasings();
        HERMETIC_CASING.setRegistryName("hermetic_casing");
        GRANITE = new BlockGranite();
        GRANITE.setRegistryName("granite");
        MINERAL = new BlockMineral();
        MINERAL.setRegistryName("mineral");

        CONCRETE = new BlockConcrete();
        CONCRETE.setRegistryName("concrete");

        FOAM = new BlockFoam(false);
        FOAM.setRegistryName("foam");
        REINFORCED_FOAM = new BlockFoam(true);
        REINFORCED_FOAM.setRegistryName("reinforced_foam");
        PETRIFIED_FOAM = new BlockPetrifiedFoam(false);
        PETRIFIED_FOAM.setRegistryName("petrified_foam");
        REINFORCED_PETRIFIED_FOAM = new BlockPetrifiedFoam(true);
        REINFORCED_PETRIFIED_FOAM.setRegistryName("reinforced_petrified_foam");

        LOG = new BlockGregLog();
        LOG.setRegistryName("log");
        LEAVES = new BlockGregLeaves();
        LEAVES.setRegistryName("leaves");
        SAPLING = new BlockGregSapling();
        SAPLING.setRegistryName("sapling");
        PLANKS = new BlockGregPlank();
        PLANKS.setRegistryName("plank");

        SURFACE_ROCK = new BlockSurfaceRock();
        SURFACE_ROCK.setRegistryName("surface_rock_new");

        StoneType.init();

        createGeneratedBlock(m -> m.hasProperty(PropertyKey.DUST) && (m.hasProperty(PropertyKey.INGOT) || m.hasProperty(PropertyKey.GEM)) && !OrePrefix.block.isIgnored(m), MetaBlocks::createCompressedBlock);
        createGeneratedBlock(m -> m.hasProperty(PropertyKey.DUST) && m.hasFlag(GENERATE_FRAME), MetaBlocks::createFrameBlock);

        createGeneratedBlock(
                material -> (material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM))
                        && !OrePrefix.block.isIgnored(material),
                MetaBlocks::createCompressedBlock);

        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.isHidden()) continue;

            if (material.hasProperty(PropertyKey.ORE)) {
                createOreBlock(material);
            }

            if (material.hasProperty(PropertyKey.WIRE)) {
                for (BlockCable cable : CABLES) {
                    if (!cable.getItemPipeType(null).isCable() || !material.getProperty(PropertyKey.WIRE).isSuperconductor)
                        cable.addCableMaterial(material, material.getProperty(PropertyKey.WIRE));
                }
            }
            if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
                for (BlockFluidPipe pipe : FLUID_PIPES)
                    pipe.addPipeMaterial(material, material.getProperty(PropertyKey.FLUID_PIPE));
            }
            if (material.hasProperty(PropertyKey.ITEM_PIPE)) {
                for (BlockItemPipe pipe : ITEM_PIPES)
                    pipe.addPipeMaterial(material, material.getProperty(PropertyKey.ITEM_PIPE));
            }
        }
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            pipe.addPipeMaterial(Materials.Wood, new FluidPipeProperties(310, 5, false));
        }
        registerTileEntity();

        //not sure if that's a good place for that, but i don't want to make a dedicated method for that
        //could possibly override block methods, but since these props don't depend on state why not just use nice and simple vanilla method
        Blocks.FIRE.setFireInfo(LOG, 5, 5);
        Blocks.FIRE.setFireInfo(LEAVES, 30, 60);
        Blocks.FIRE.setFireInfo(PLANKS, 5, 20);
    }

    /**
     * Deterministically populates a category of MetaBlocks based on the unique registry ID of each qualifying Material.
     *
     * @param materialPredicate a filter for determining if a Material qualifies for generation in the category.
     * @param blockGenerator    a function which accepts a Materials set to pack into a MetaBlock, and the ordinal this
     *                          MetaBlock should have within its category.
     */
    protected static void createGeneratedBlock(Predicate<Material> materialPredicate,
                                               BiConsumer<Material[], Integer> blockGenerator) {

        Map<Integer, Material[]> blocksToGenerate = new TreeMap<>();

        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            if (material.isHidden()) continue;
            if (materialPredicate.test(material)) {
                int id = material.getId();
                int metaBlockID = id / 16;
                int subBlockID = id % 16;

                if (!blocksToGenerate.containsKey(metaBlockID)) {
                    Material[] materials = new Material[16];
                    Arrays.fill(materials, Materials._NULL);
                    blocksToGenerate.put(metaBlockID, materials);
                }

                blocksToGenerate.get(metaBlockID)[subBlockID] = material;
            }
        }

        blocksToGenerate.forEach((key, value) -> blockGenerator.accept(value, key));
    }

    private static void createCompressedBlock(Material[] materials, int index) {
        BlockCompressed block = new BlockCompressed(materials);
        block.setRegistryName("meta_block_compressed_" + index);
        for (Material material : materials) {
            COMPRESSED.put(material, block);
        }
    }

    private static void createFrameBlock(Material[] materials, int index) {
        BlockFrame block = new BlockFrame(materials);
        block.setRegistryName("meta_block_frame_" + index);
        for (Material m : materials) {
            FRAMES.put(m, block);
        }
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

    public static void registerTileEntity() {
        GameRegistry.registerTileEntity(MetaTileEntityHolder.class, new ResourceLocation(GTValues.MODID, "machine"));
        GameRegistry.registerTileEntity(TileEntityCable.class, new ResourceLocation(GTValues.MODID, "cable"));
        GameRegistry.registerTileEntity(TileEntityCableTickable.class, new ResourceLocation(GTValues.MODID, "cable_tickable"));
        GameRegistry.registerTileEntity(TileEntityFluidPipe.class, new ResourceLocation(GTValues.MODID, "fluid_pipe"));
        GameRegistry.registerTileEntity(TileEntityItemPipe.class, new ResourceLocation(GTValues.MODID, "item_pipe"));
        GameRegistry.registerTileEntity(TileEntityFluidPipeTickable.class, new ResourceLocation(GTValues.MODID, "fluid_pipe_active"));
        GameRegistry.registerTileEntity(TileEntityItemPipeTickable.class, new ResourceLocation(GTValues.MODID, "item_pipe_active"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MACHINE), stack -> MetaTileEntityRenderer.MODEL_LOCATION);
        for (BlockCable cable : CABLES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(cable), stack -> CableRenderer.MODEL_LOCATION);
        for (BlockFluidPipe pipe : FLUID_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> FluidPipeRenderer.MODEL_LOCATION);
        for (BlockItemPipe pipe : ITEM_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> ItemPipeRenderer.MODEL_LOCATION);
        registerItemModel(BOILER_CASING);
        registerItemModel(BOILER_FIREBOX_CASING);
        registerItemModel(METAL_CASING);
        registerItemModel(TURBINE_CASING);
        registerItemModel(MACHINE_CASING);
        registerItemModel(STEAM_CASING);
        registerItemModel(MULTIBLOCK_CASING);
        registerItemModel(TRANSPARENT_CASING);
        registerItemModel(WIRE_COIL);
        registerItemModel(WIRE_COIL2);
        registerItemModel(FUSION_CASING);
        registerItemModel(WARNING_SIGN);
        registerItemModel(HERMETIC_CASING);
        registerItemModel(GRANITE);
        registerItemModel(MINERAL);
        registerItemModel(CONCRETE);
        registerItemModelWithOverride(LOG, ImmutableMap.of(BlockGregLog.LOG_AXIS, EnumAxis.Y));
        registerItemModel(LEAVES);
        registerItemModel(SAPLING);
        registerItemModel(PLANKS);

        COMPRESSED.values().stream().distinct().forEach(IModelSupplier::onModelRegister);
        FRAMES.values().stream().distinct().forEach(IModelSupplier::onModelRegister);
        ORES.forEach(IModelSupplier::onModelRegister);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModelWithFilteredProperties(Block block, IProperty<?>... filteredProperties) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            HashMap<IProperty<?>, Comparable<?>> stringProperties = new HashMap<>();
            for (IProperty<?> property : filteredProperties) {
                stringProperties.put(property, state.getValue(property));
            }
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(stringProperties)));
        }
    }


    @SideOnly(Side.CLIENT)
    private static void registerItemModelWithOverride(Block block, Map<IProperty<?>, Comparable<?>> stateOverrides) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            HashMap<IProperty<?>, Comparable<?>> stringProperties = new HashMap<>(state.getProperties());
            stringProperties.putAll(stateOverrides);
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(stringProperties)));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerStateMappers() {
        ModelLoader.setCustomStateMapper(MACHINE, new DefaultStateMapper() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                return MetaTileEntityRenderer.MODEL_LOCATION;
            }
        });

        for (BlockCable cable : CABLES) {
            ModelLoader.setCustomStateMapper(cable, new DefaultStateMapper() {
                @Nonnull
                @Override
                protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                    return CableRenderer.MODEL_LOCATION;
                }
            });
        }
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, new DefaultStateMapper() {
                @Nonnull
                @Override
                protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                    return FluidPipeRenderer.MODEL_LOCATION;
                }
            });
        }
        for (BlockItemPipe pipe : ITEM_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, new DefaultStateMapper() {
                @Nonnull
                @Override
                protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
                    return ItemPipeRenderer.MODEL_LOCATION;
                }
            });
        }

        IStateMapper normalStateMapper = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), "normal");
            }
        };
        ModelLoader.setCustomStateMapper(FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(PETRIFIED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_PETRIFIED_FOAM, normalStateMapper);

        BakedModelHandler modelHandler = new BakedModelHandler();
        MinecraftForge.EVENT_BUS.register(modelHandler);
        FLUID_BLOCKS.forEach(modelHandler::addFluidBlock);

        modelHandler.addBuiltInBlock(SURFACE_ROCK, "stone_andesite");

        ClientRegistry.bindTileEntitySpecialRenderer(MetaTileEntityHolder.class, new MetaTileEntityTESR());
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                FOAM_BLOCK_COLOR, FOAM, REINFORCED_FOAM, PETRIFIED_FOAM, REINFORCED_PETRIFIED_FOAM);

        MetaBlocks.COMPRESSED.values().stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(COMPRESSED_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(COMPRESSED_ITEM_COLOR, block);
        });

        MetaBlocks.FRAMES.values().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(FRAME_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(FRAME_ITEM_COLOR, block);
        });

        MetaBlocks.ORES.stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(ORE_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ORE_ITEM_COLOR, block);
        });
    }

    public static void registerOreDict() {
        OreDictUnifier.registerOre(new ItemStack(LOG, 1, GTValues.W), OrePrefix.log, Materials.Wood);
        OreDictUnifier.registerOre(new ItemStack(LEAVES, 1, GTValues.W), "treeLeaves");
        OreDictUnifier.registerOre(new ItemStack(SAPLING, 1, GTValues.W), "treeSapling");
        OreDictUnifier.registerOre(new ItemStack(PLANKS, 1, GTValues.W), OrePrefix.plank, Materials.Wood);
        GameRegistry.addSmelting(LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);

        for (Entry<Material, BlockCompressed> entry : COMPRESSED.entrySet()) {
            Material material = entry.getKey();
            BlockCompressed block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            OreDictUnifier.registerOre(itemStack, OrePrefix.block, material);
        }

        for (Entry<Material, BlockFrame> entry : FRAMES.entrySet()) {
            Material material = entry.getKey();
            BlockFrame block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            OreDictUnifier.registerOre(itemStack, OrePrefix.frameGt, material);
        }

        for (BlockOre blockOre : ORES) {
            Material material = blockOre.material;
            for (StoneType stoneType : blockOre.STONE_TYPE.getAllowedValues()) {
                if (stoneType == null || !stoneType.shouldBeDroppedAsItem) continue;
                ItemStack normalStack = blockOre.getItem(blockOre.getDefaultState()
                        .withProperty(blockOre.STONE_TYPE, stoneType));
                OreDictUnifier.registerOre(normalStack, stoneType.processingPrefix, material);
            }
        }
        for (BlockCable cable : CABLES) {
            for (Material pipeMaterial : cable.getEnabledMaterials()) {
                ItemStack itemStack = cable.getItem(pipeMaterial);
                OreDictUnifier.registerOre(itemStack, cable.getPrefix(), pipeMaterial);
            }
        }
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                ItemStack itemStack = pipe.getItem(pipeMaterial);
                OreDictUnifier.registerOre(itemStack, pipe.getPrefix(), pipeMaterial);
            }
        }
        for (BlockItemPipe pipe : ITEM_PIPES) {
            for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                ItemStack itemStack = pipe.getItem(pipeMaterial);
                OreDictUnifier.registerOre(itemStack, pipe.getPrefix(), pipeMaterial);
            }
        }
    }

    public static String statePropertiesToString(Map<IProperty<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Entry<IProperty<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
