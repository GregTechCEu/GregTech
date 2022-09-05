package gregtech.common.blocks;

import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.client.model.modelfactories.BakedModelHandler;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.handler.MetaTileEntityTESR;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.common.blocks.foam.BlockFoam;
import gregtech.common.blocks.foam.BlockPetrifiedFoam;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.common.blocks.wood.BlockRubberLeaves;
import gregtech.common.blocks.wood.BlockRubberLog;
import gregtech.common.blocks.wood.BlockRubberSapling;
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
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.info.MaterialFlags.FORCE_GENERATE_BLOCK;
import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FRAME;
import static gregtech.client.ClientProxy.*;

public class MetaBlocks {

    private MetaBlocks() {
    }

    public static BlockMachine MACHINE;
    public static final BlockCable[] CABLES = new BlockCable[10];
    public static final BlockFluidPipe[] FLUID_PIPES = new BlockFluidPipe[7];
    public static final BlockItemPipe[] ITEM_PIPES = new BlockItemPipe[8];

    public static BlockBoilerCasing BOILER_CASING;
    public static BlockFireboxCasing BOILER_FIREBOX_CASING;
    public static BlockMetalCasing METAL_CASING;
    public static BlockTurbineCasing TURBINE_CASING;
    public static BlockMachineCasing MACHINE_CASING;
    public static BlockSteamCasing STEAM_CASING;
    public static BlockMultiblockCasing MULTIBLOCK_CASING;
    public static BlockGlassCasing TRANSPARENT_CASING;
    public static BlockWireCoil WIRE_COIL;
    public static BlockFusionCasing FUSION_CASING;
    public static BlockWarningSign WARNING_SIGN;
    public static BlockWarningSign1 WARNING_SIGN_1;
    public static BlockHermeticCasing HERMETIC_CASING;
    public static BlockCleanroomCasing CLEANROOM_CASING;

    public static BlockAsphalt ASPHALT;

    public static BlockStoneSmooth STONE_SMOOTH;
    public static BlockStoneCobble STONE_COBBLE;
    public static BlockStoneCobbleMossy STONE_COBBLE_MOSSY;
    public static BlockStonePolished STONE_POLISHED;
    public static BlockStoneBricks STONE_BRICKS;
    public static BlockStoneBricksCracked STONE_BRICKS_CRACKED;
    public static BlockStoneBricksMossy STONE_BRICKS_MOSSY;
    public static BlockStoneChiseled STONE_CHISELED;
    public static BlockStoneTiled STONE_TILED;
    public static BlockStoneTiledSmall STONE_TILED_SMALL;
    public static BlockStoneBricksSmall STONE_BRICKS_SMALL;
    public static BlockStoneWindmillA STONE_WINDMILL_A;
    public static BlockStoneWindmillB STONE_WINDMILL_B;
    public static BlockStoneBricksSquare STONE_BRICKS_SQUARE;

    public static BlockFoam FOAM;
    public static BlockFoam REINFORCED_FOAM;
    public static BlockPetrifiedFoam PETRIFIED_FOAM;
    public static BlockPetrifiedFoam REINFORCED_PETRIFIED_FOAM;

    public static BlockRubberLog RUBBER_LOG;
    public static BlockRubberLeaves RUBBER_LEAVES;
    public static BlockRubberSapling RUBBER_SAPLING;
    public static BlockGregPlanks PLANKS;

    public static final Map<Material, BlockCompressed> COMPRESSED = new HashMap<>();
    public static final Map<Material, BlockFrame> FRAMES = new HashMap<>();
    public static final Collection<BlockOre> ORES = new ReferenceArrayList<>();
    public static final Map<Material, BlockSurfaceRock> SURFACE_ROCK = new HashMap<>();
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
        FUSION_CASING = new BlockFusionCasing();
        FUSION_CASING.setRegistryName("fusion_casing");
        WARNING_SIGN = new BlockWarningSign();
        WARNING_SIGN.setRegistryName("warning_sign");
        WARNING_SIGN_1 = new BlockWarningSign1();
        WARNING_SIGN_1.setRegistryName("warning_sign_1");
        HERMETIC_CASING = new BlockHermeticCasing();
        HERMETIC_CASING.setRegistryName("hermetic_casing");
        CLEANROOM_CASING = new BlockCleanroomCasing();
        CLEANROOM_CASING.setRegistryName("cleanroom_casing");

        ASPHALT = new BlockAsphalt();
        ASPHALT.setRegistryName("asphalt");

        STONE_SMOOTH = new BlockStoneSmooth();
        STONE_SMOOTH.setRegistryName("stone_smooth");
        STONE_COBBLE = new BlockStoneCobble();
        STONE_COBBLE.setRegistryName("stone_cobble");
        STONE_COBBLE_MOSSY = new BlockStoneCobbleMossy();
        STONE_COBBLE_MOSSY.setRegistryName("stone_cobble_mossy");
        STONE_POLISHED = new BlockStonePolished();
        STONE_POLISHED.setRegistryName("stone_polished");
        STONE_BRICKS = new BlockStoneBricks();
        STONE_BRICKS.setRegistryName("stone_bricks");
        STONE_BRICKS_CRACKED = new BlockStoneBricksCracked();
        STONE_BRICKS_CRACKED.setRegistryName("stone_bricks_cracked");
        STONE_BRICKS_MOSSY = new BlockStoneBricksMossy();
        STONE_BRICKS_MOSSY.setRegistryName("stone_bricks_mossy");
        STONE_CHISELED = new BlockStoneChiseled();
        STONE_CHISELED.setRegistryName("stone_chiseled");
        STONE_TILED = new BlockStoneTiled();
        STONE_TILED.setRegistryName("stone_tiled");
        STONE_TILED_SMALL = new BlockStoneTiledSmall();
        STONE_TILED_SMALL.setRegistryName("stone_tiled_small");
        STONE_BRICKS_SMALL = new BlockStoneBricksSmall();
        STONE_BRICKS_SMALL.setRegistryName("stone_bricks_small");
        STONE_WINDMILL_A = new BlockStoneWindmillA();
        STONE_WINDMILL_A.setRegistryName("stone_windmill_a");
        STONE_WINDMILL_B = new BlockStoneWindmillB();
        STONE_WINDMILL_B.setRegistryName("stone_windmill_b");
        STONE_BRICKS_SQUARE = new BlockStoneBricksSquare();
        STONE_BRICKS_SQUARE.setRegistryName("stone_bricks_square");

        FOAM = new BlockFoam(false);
        FOAM.setRegistryName("foam");
        REINFORCED_FOAM = new BlockFoam(true);
        REINFORCED_FOAM.setRegistryName("reinforced_foam");
        PETRIFIED_FOAM = new BlockPetrifiedFoam(false);
        PETRIFIED_FOAM.setRegistryName("petrified_foam");
        REINFORCED_PETRIFIED_FOAM = new BlockPetrifiedFoam(true);
        REINFORCED_PETRIFIED_FOAM.setRegistryName("reinforced_petrified_foam");

        RUBBER_LOG = new BlockRubberLog();
        RUBBER_LOG.setRegistryName("rubber_log");
        RUBBER_LEAVES = new BlockRubberLeaves();
        RUBBER_LEAVES.setRegistryName("rubber_leaves");
        RUBBER_SAPLING = new BlockRubberSapling();
        RUBBER_SAPLING.setRegistryName("rubber_sapling");
        PLANKS = new BlockGregPlanks();
        PLANKS.setRegistryName("planks");

        createGeneratedBlock(m -> m.hasProperty(PropertyKey.DUST) && m.hasFlag(GENERATE_FRAME), MetaBlocks::createFrameBlock);
        createGeneratedBlock(m -> m.hasProperty(PropertyKey.ORE) && m.hasProperty(PropertyKey.DUST), MetaBlocks::createSurfaceRockBlock);

        createGeneratedBlock(
                material -> (material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(FORCE_GENERATE_BLOCK))
                        && !OrePrefix.block.isIgnored(material),
                MetaBlocks::createCompressedBlock);


        registerTileEntity();

        //not sure if that's a good place for that, but i don't want to make a dedicated method for that
        //could possibly override block methods, but since these props don't depend on state why not just use nice and simple vanilla method
        Blocks.FIRE.setFireInfo(RUBBER_LOG, 5, 5);
        Blocks.FIRE.setFireInfo(RUBBER_LEAVES, 30, 60);
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
            if (materialPredicate.test(material)) {
                int id = material.getId();
                int metaBlockID = id / 16;
                int subBlockID = id % 16;

                if (!blocksToGenerate.containsKey(metaBlockID)) {
                    Material[] materials = new Material[16];
                    Arrays.fill(materials, Materials.NULL);
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

    private static void createSurfaceRockBlock(Material[] materials, int index) {
        BlockSurfaceRock block = new BlockSurfaceRock(materials);
        block.setRegistryName("meta_block_surface_rock_" + index);
        for (Material material : materials) {
            SURFACE_ROCK.put(material, block);
        }
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
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(cable), stack -> CableRenderer.INSTANCE.getModelLocation());
        for (BlockFluidPipe pipe : FLUID_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> FluidPipeRenderer.INSTANCE.getModelLocation());
        for (BlockItemPipe pipe : ITEM_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> ItemPipeRenderer.INSTANCE.getModelLocation());
        registerItemModel(BOILER_CASING);
        registerItemModel(METAL_CASING);
        registerItemModel(TURBINE_CASING);
        registerItemModel(MACHINE_CASING);
        registerItemModel(STEAM_CASING);
        registerItemModel(WARNING_SIGN);
        registerItemModel(WARNING_SIGN_1);
        registerItemModel(HERMETIC_CASING);
        registerItemModel(CLEANROOM_CASING);
        registerItemModel(ASPHALT);
        registerItemModel(STONE_SMOOTH);
        registerItemModel(STONE_COBBLE);
        registerItemModel(STONE_COBBLE_MOSSY);
        registerItemModel(STONE_POLISHED);
        registerItemModel(STONE_BRICKS);
        registerItemModel(STONE_BRICKS_CRACKED);
        registerItemModel(STONE_BRICKS_MOSSY);
        registerItemModel(STONE_CHISELED);
        registerItemModel(STONE_TILED);
        registerItemModel(STONE_TILED_SMALL);
        registerItemModel(STONE_BRICKS_SMALL);
        registerItemModel(STONE_WINDMILL_A);
        registerItemModel(STONE_WINDMILL_B);
        registerItemModel(STONE_BRICKS_SQUARE);
        registerItemModelWithOverride(RUBBER_LOG, ImmutableMap.of(BlockRubberLog.LOG_AXIS, EnumAxis.Y));
        registerItemModel(RUBBER_LEAVES);
        registerItemModel(RUBBER_SAPLING);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_SAPLING), 0,
                new ModelResourceLocation(RUBBER_SAPLING.getRegistryName(), "inventory"));
        registerItemModel(PLANKS);

        BOILER_FIREBOX_CASING.onModelRegister();
        WIRE_COIL.onModelRegister();
        FUSION_CASING.onModelRegister();
        MULTIBLOCK_CASING.onModelRegister();
        TRANSPARENT_CASING.onModelRegister();

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
        ModelLoader.setCustomStateMapper(MACHINE, new SimpleStateMapper(MetaTileEntityRenderer.MODEL_LOCATION));

        IStateMapper normalStateMapper = new SimpleStateMapper(CableRenderer.INSTANCE.getModelLocation());
        for (BlockCable cable : CABLES) {
            ModelLoader.setCustomStateMapper(cable, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(FluidPipeRenderer.INSTANCE.getModelLocation());
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(ItemPipeRenderer.INSTANCE.getModelLocation());
        for (BlockItemPipe pipe : ITEM_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(BlockSurfaceRock.MODEL_LOCATION);
        for (BlockSurfaceRock surfaceRock : new HashSet<>(SURFACE_ROCK.values())) {
            ModelLoader.setCustomStateMapper(surfaceRock, normalStateMapper);
        }

        normalStateMapper = new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
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

        ClientRegistry.bindTileEntitySpecialRenderer(MetaTileEntityHolder.class, new MetaTileEntityTESR());
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                FOAM_BLOCK_COLOR, FOAM, REINFORCED_FOAM, PETRIFIED_FOAM, REINFORCED_PETRIFIED_FOAM);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(RUBBER_LEAVES_BLOCK_COLOR, RUBBER_LEAVES);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(RUBBER_LEAVES_ITEM_COLOR, RUBBER_LEAVES);

        MetaBlocks.COMPRESSED.values().stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(COMPRESSED_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(COMPRESSED_ITEM_COLOR, block);
        });

        MetaBlocks.FRAMES.values().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(FRAME_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(FRAME_ITEM_COLOR, block);
        });

        MetaBlocks.SURFACE_ROCK.values().stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(SURFACE_ROCK_BLOCK_COLOR, block);
        });

        MetaBlocks.ORES.stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(ORE_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ORE_ITEM_COLOR, block);
        });

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(MACHINE_CASING_BLOCK_COLOR, MACHINE_CASING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(MACHINE_CASING_ITEM_COLOR, MACHINE_CASING);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(MACHINE_CASING_BLOCK_COLOR, HERMETIC_CASING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(MACHINE_CASING_ITEM_COLOR, HERMETIC_CASING);
    }

    public static void registerOreDict() {
        OreDictUnifier.registerOre(new ItemStack(RUBBER_LOG, 1, GTValues.W), OrePrefix.log, Materials.Wood);
        OreDictUnifier.registerOre(new ItemStack(RUBBER_LEAVES, 1, GTValues.W), "treeLeaves");
        OreDictUnifier.registerOre(new ItemStack(RUBBER_SAPLING, 1, GTValues.W), "treeSapling");
        OreDictUnifier.registerOre(PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), OrePrefix.plank, Materials.Wood);
        OreDictUnifier.registerOre(PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), new ItemMaterialInfo(new MaterialStack(Materials.Wood, GTValues.M)));
        OreDictUnifier.registerOre(PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), OrePrefix.plank, Materials.TreatedWood);
        OreDictUnifier.registerOre(PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), new ItemMaterialInfo(new MaterialStack(Materials.TreatedWood, GTValues.M)));
        GameRegistry.addSmelting(RUBBER_LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);

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
                if (stoneType == null) continue;
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
