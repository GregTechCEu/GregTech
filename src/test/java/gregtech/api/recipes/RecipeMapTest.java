package gregtech.api.recipes;

import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

public class RecipeMapTest {

    @BeforeClass
    public static void init() {
        Bootstrap.register();
    }

    @Test
    public void findRecipe() {
        RecipeMap<SimpleRecipeBuilder> map = new RecipeMap<>("chemical_reactor",
                0,
                3,
                0,
                2,
                0,
                3,
                0,
                2,
                new SimpleRecipeBuilder().EUt(30),
                false);

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.OBSIDIAN))
                .inputs(new ItemStack(Blocks.STONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(2).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(FluidRegistry.WATER)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        //assertEquals(3, map.getRecipeList().size());
        Block[] blocks = new Block[]{
                Blocks.STONE,
                Blocks.DIRT,
                Blocks.COBBLESTONE,
                Blocks.PLANKS,
                Blocks.SAPLING,
                Blocks.BEDROCK,
                Blocks.GRAVEL,
                Blocks.GOLD_ORE,
                Blocks.IRON_ORE,
                Blocks.COAL_ORE,
                Blocks.LOG,
                Blocks.LOG2,
                Blocks.SPONGE,
                Blocks.GLASS,
                Blocks.LAPIS_ORE,
                Blocks.LAPIS_BLOCK,
                Blocks.DISPENSER,
                Blocks.SANDSTONE,
                Blocks.NOTEBLOCK,
                Blocks.BED,
                Blocks.GOLDEN_RAIL,
                Blocks.DETECTOR_RAIL,
                Blocks.WEB,
                Blocks.WOOL,
                Blocks.GOLD_BLOCK,
                Blocks.IRON_BLOCK,
                Blocks.BRICK_BLOCK,
                Blocks.TNT,
                Blocks.BOOKSHELF,
                Blocks.MOSSY_COBBLESTONE,
                Blocks.OBSIDIAN,
                Blocks.TORCH,
                Blocks.MOB_SPAWNER,
                Blocks.OAK_STAIRS,
                Blocks.DIAMOND_ORE,
                Blocks.DIAMOND_BLOCK,
                Blocks.CRAFTING_TABLE,
                Blocks.WHEAT,
                Blocks.FARMLAND,
                Blocks.FURNACE,
                Blocks.LIT_FURNACE,
                Blocks.STANDING_SIGN,
                Blocks.LADDER,
                Blocks.RAIL,
                Blocks.STONE_STAIRS,
                Blocks.WALL_SIGN,
                Blocks.LEVER,
                Blocks.STONE_PRESSURE_PLATE,
                Blocks.WOODEN_PRESSURE_PLATE,
                Blocks.REDSTONE_ORE,
                Blocks.LIT_REDSTONE_ORE,
                Blocks.UNLIT_REDSTONE_TORCH,
                Blocks.REDSTONE_TORCH,
                Blocks.STONE_BUTTON,
                Blocks.SNOW_LAYER,
                Blocks.ICE,
                Blocks.SNOW,
                Blocks.CLAY,
                Blocks.JUKEBOX,
                Blocks.OAK_FENCE,
                Blocks.SPRUCE_FENCE,
                Blocks.BIRCH_FENCE,
                Blocks.JUNGLE_FENCE,
                Blocks.DARK_OAK_FENCE,
                Blocks.ACACIA_FENCE,
                Blocks.PUMPKIN,
                Blocks.NETHERRACK,
                Blocks.SOUL_SAND,
                Blocks.GLOWSTONE,
                Blocks.LIT_PUMPKIN,
                Blocks.CAKE,
                Blocks.TRAPDOOR,
                Blocks.MONSTER_EGG,
                Blocks.STONEBRICK,
                Blocks.BROWN_MUSHROOM_BLOCK,
                Blocks.RED_MUSHROOM_BLOCK,
                Blocks.IRON_BARS,
                Blocks.GLASS_PANE,
                Blocks.MELON_BLOCK,
                Blocks.PUMPKIN_STEM,
                Blocks.MELON_STEM,
                Blocks.VINE,
                Blocks.OAK_FENCE_GATE,
                Blocks.SPRUCE_FENCE_GATE,
                Blocks.BIRCH_FENCE_GATE,
                Blocks.JUNGLE_FENCE_GATE,
                Blocks.DARK_OAK_FENCE_GATE,
                Blocks.ACACIA_FENCE_GATE,
                Blocks.BRICK_STAIRS,
                Blocks.STONE_BRICK_STAIRS,
                Blocks.WATERLILY,
                Blocks.NETHER_BRICK,
                Blocks.NETHER_BRICK_FENCE,
                Blocks.NETHER_BRICK_STAIRS,
                Blocks.NETHER_WART,
                Blocks.ENCHANTING_TABLE,
                Blocks.BREWING_STAND,
                Blocks.END_PORTAL,
                Blocks.END_PORTAL_FRAME,
                Blocks.END_STONE,
                Blocks.DRAGON_EGG,
                Blocks.REDSTONE_LAMP,
                Blocks.LIT_REDSTONE_LAMP,
                Blocks.COCOA,
                Blocks.SANDSTONE_STAIRS,
                Blocks.EMERALD_ORE,
                Blocks.ENDER_CHEST,
                Blocks.TRIPWIRE,
                Blocks.EMERALD_BLOCK,
                Blocks.SPRUCE_STAIRS,
                Blocks.BIRCH_STAIRS,
                Blocks.JUNGLE_STAIRS,
                Blocks.COMMAND_BLOCK,
                Blocks.COBBLESTONE_WALL,
                Blocks.FLOWER_POT,
                Blocks.CARROTS,
                Blocks.POTATOES,
                Blocks.WOODEN_BUTTON,
                Blocks.ANVIL,
                Blocks.TRAPPED_CHEST,
                Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Blocks.REDSTONE_BLOCK,
                Blocks.QUARTZ_ORE,
                Blocks.QUARTZ_BLOCK,
                Blocks.QUARTZ_STAIRS,
                Blocks.ACTIVATOR_RAIL,
                Blocks.DROPPER,
                Blocks.STAINED_HARDENED_CLAY,
                Blocks.BARRIER,
                Blocks.IRON_TRAPDOOR,
                Blocks.HAY_BLOCK,
                Blocks.CARPET,
                Blocks.HARDENED_CLAY,
                Blocks.COAL_BLOCK,
                Blocks.PACKED_ICE,
                Blocks.ACACIA_STAIRS,
                Blocks.DARK_OAK_STAIRS,
                Blocks.SLIME_BLOCK,
                Blocks.PRISMARINE,
                Blocks.SEA_LANTERN,
                Blocks.STANDING_BANNER,
                Blocks.WALL_BANNER,
                Blocks.RED_SANDSTONE,
                Blocks.RED_SANDSTONE_STAIRS,
                Blocks.END_ROD,
                Blocks.CHORUS_PLANT,
                Blocks.CHORUS_FLOWER,
                Blocks.PURPUR_BLOCK,
                Blocks.PURPUR_PILLAR,
                Blocks.PURPUR_STAIRS,
                Blocks.END_BRICKS,
                Blocks.BEETROOTS,
                Blocks.GRASS_PATH,
                Blocks.END_GATEWAY,
                Blocks.REPEATING_COMMAND_BLOCK,
                Blocks.CHAIN_COMMAND_BLOCK,
                Blocks.FROSTED_ICE,
                Blocks.MAGMA,
                Blocks.NETHER_WART_BLOCK,
                Blocks.RED_NETHER_BRICK,
                Blocks.BONE_BLOCK,
                Blocks.STRUCTURE_VOID,
                Blocks.OBSERVER,
                Blocks.WHITE_SHULKER_BOX,
                Blocks.ORANGE_SHULKER_BOX,
                Blocks.MAGENTA_SHULKER_BOX,
                Blocks.LIGHT_BLUE_SHULKER_BOX,
                Blocks.YELLOW_SHULKER_BOX,
                Blocks.LIME_SHULKER_BOX,
                Blocks.PINK_SHULKER_BOX,
                Blocks.GRAY_SHULKER_BOX,
                Blocks.SILVER_SHULKER_BOX,
                Blocks.CYAN_SHULKER_BOX,
                Blocks.PURPLE_SHULKER_BOX,
                Blocks.BLUE_SHULKER_BOX,
                Blocks.BROWN_SHULKER_BOX,
                Blocks.GREEN_SHULKER_BOX,
                Blocks.RED_SHULKER_BOX,
                Blocks.BLACK_SHULKER_BOX,
                Blocks.WHITE_GLAZED_TERRACOTTA,
                Blocks.ORANGE_GLAZED_TERRACOTTA,
                Blocks.MAGENTA_GLAZED_TERRACOTTA,
                Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
                Blocks.YELLOW_GLAZED_TERRACOTTA,
                Blocks.LIME_GLAZED_TERRACOTTA,
                Blocks.PINK_GLAZED_TERRACOTTA,
                Blocks.GRAY_GLAZED_TERRACOTTA,
                Blocks.SILVER_GLAZED_TERRACOTTA,
                Blocks.CYAN_GLAZED_TERRACOTTA,
                Blocks.PURPLE_GLAZED_TERRACOTTA,
                Blocks.BLUE_GLAZED_TERRACOTTA,
                Blocks.BROWN_GLAZED_TERRACOTTA,
                Blocks.GREEN_GLAZED_TERRACOTTA,
                Blocks.RED_GLAZED_TERRACOTTA,
                Blocks.BLACK_GLAZED_TERRACOTTA,
                Blocks.CONCRETE,
                Blocks.CONCRETE_POWDER,
                Blocks.STRUCTURE_BLOCK};


        for (Block bl1 : blocks){
            ItemStack i1 = new ItemStack(bl1);
            if (!i1.isEmpty()) {
                for (Block bl2 : blocks) {
                    ItemStack i2 = new ItemStack(bl2);
                    if (!i2.isEmpty()) {
                        map.recipeBuilder()
                                .inputs(i1)
                                .inputs(i2)
                                .outputs(new ItemStack(Blocks.STONE))
                                .EUt(1).duration(1)
                                .buildAndRegister();
                    }
                }
            }
        }

        ItemStack is = new ItemStack(Blocks.STONE);

        is.setCount(1);
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE))
                .inputs(new ItemStack(Blocks.OBSIDIAN))
                .inputs(new ItemStack(Blocks.REDSTONE_BLOCK))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        List<ItemStack> il1 = Collections.singletonList(is);
        List<FluidStack> fl1 = Collections.singletonList(null);
        StopWatch watch = StopWatch.createStarted();
        for (int i = 1; i < 100000; i++) {
            is.setCount(100000 - i );
            map.findRecipe(1, il1, fl1, 0, MatchingMode.DEFAULT);
        }
        watch.stop();
        System.out.println(watch.getTime(TimeUnit.MICROSECONDS) + "us");

        watch.reset();
        List<ItemStack> il2 = Collections.singletonList(new ItemStack(Blocks.STONE));
        List<FluidStack> fl2 = Collections.singletonList(new FluidStack(FluidRegistry.WATER,1));
        watch.start();
        Recipe r2 = map.findRecipe(1, il2 , fl2, 0, MatchingMode.DEFAULT);
        watch.stop();
        System.out.println(watch.getTime(TimeUnit.MICROSECONDS) + "us 2");


        ItemStack srs = new ItemStack(Blocks.OBSIDIAN);
        ItemStack srs2 = new ItemStack(Blocks.STONE);
        ItemStack srs3 = new ItemStack(Blocks.REDSTONE_BLOCK);

        ArrayList<ItemStack> il = new ArrayList<>();
        il.add(srs);
        il.add(srs2);
        il.add(srs3);


        watch.reset();
        watch.start();
        Recipe r3 = map.findRecipe(1, il, Collections.singletonList(null), 0, MatchingMode.DEFAULT);
        watch.stop();
        System.out.println(watch.getTime(TimeUnit.MICROSECONDS) + "us 3");
        assertNotNull(r2);
    }

}
