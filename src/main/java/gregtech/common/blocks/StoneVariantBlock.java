package gregtech.common.blocks;

import gregtech.api.block.IStateSpawnControl;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.creativetab.GTCreativeTabs;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

@SuppressWarnings("deprecation")
public class StoneVariantBlock extends VariantBlock<StoneVariantBlock.StoneType> {

    private final StoneVariant stoneVariant;

    public StoneVariantBlock(@NotNull StoneVariant stoneVariant) {
        super(net.minecraft.block.material.Material.ROCK);
        this.stoneVariant = stoneVariant;
        setRegistryName(stoneVariant.id);
        setTranslationKey(stoneVariant.translationKey);
        setHardness(stoneVariant.hardness);
        setResistance(stoneVariant.resistance);
        setSoundType(SoundType.STONE);
        setHarvestLevel(ToolClasses.PICKAXE, 0);
        setDefaultState(getState(StoneType.BLACK_GRANITE));
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_DECORATIONS);
    }

    @Override
    protected boolean canSilkHarvest() {
        return this.stoneVariant == StoneVariant.SMOOTH;
    }

    @NotNull
    @Override
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Item.getItemFromBlock(this.stoneVariant == StoneVariant.SMOOTH ?
                MetaBlocks.STONE_BLOCKS.get(StoneVariant.COBBLE) : this);
    }

    public enum StoneType implements IStringSerializable, IStateSpawnControl {

        BLACK_GRANITE("black_granite", MapColor.BLACK),
        RED_GRANITE("red_granite", MapColor.RED),
        MARBLE("marble", MapColor.QUARTZ),
        BASALT("basalt", MapColor.BLACK_STAINED_HARDENED_CLAY),
        CONCRETE_LIGHT("concrete_light", MapColor.STONE, false),
        CONCRETE_DARK("concrete_dark", MapColor.STONE, false);

        private final String name;
        private final boolean allowSpawn;
        public final MapColor mapColor;

        StoneType(@NotNull String name, @NotNull MapColor mapColor) {
            this(name, mapColor, true);
        }

        StoneType(@NotNull String name, @NotNull MapColor mapColor, boolean allowSpawn) {
            this.name = name;
            this.mapColor = mapColor;
            this.allowSpawn = allowSpawn;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public OrePrefix getOrePrefix() {
            return switch (this) {
                case BLACK_GRANITE, RED_GRANITE, MARBLE, BASALT -> OrePrefix.stone;
                case CONCRETE_LIGHT, CONCRETE_DARK -> OrePrefix.block;
            };
        }

        public Material getMaterial() {
            return switch (this) {
                case BLACK_GRANITE -> Materials.GraniteBlack;
                case RED_GRANITE -> Materials.GraniteRed;
                case MARBLE -> Materials.Marble;
                case BASALT -> Materials.Basalt;
                case CONCRETE_LIGHT, CONCRETE_DARK -> Materials.Concrete;
            };
        }

        @Override
        public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                        @NotNull EntityLiving.SpawnPlacementType type) {
            return this.allowSpawn;
        }
    }

    public enum StoneVariant {

        SMOOTH("stone_smooth"),
        COBBLE("stone_cobble", 2.0f, 10.0f),
        COBBLE_MOSSY("stone_cobble_mossy", 2.0f, 10.0f),
        POLISHED("stone_polished"),
        BRICKS("stone_bricks"),
        BRICKS_CRACKED("stone_bricks_cracked"),
        BRICKS_MOSSY("stone_bricks_mossy"),
        CHISELED("stone_chiseled"),
        TILED("stone_tiled"),
        TILED_SMALL("stone_tiled_small"),
        BRICKS_SMALL("stone_bricks_small"),
        WINDMILL_A("stone_windmill_a", "stone_bricks_windmill_a"),
        WINDMILL_B("stone_windmill_b", "stone_bricks_windmill_b"),
        BRICKS_SQUARE("stone_bricks_square");

        public final String id;
        public final String translationKey;
        public final float hardness;
        public final float resistance;

        StoneVariant(@NotNull String id) {
            this(id, id);
        }

        StoneVariant(@NotNull String id, @NotNull String translationKey) {
            this(id, translationKey, 1.5f, 10.0f); // vanilla stone stats
        }

        StoneVariant(@NotNull String id, float hardness, float resistance) {
            this(id, id, hardness, resistance);
        }

        StoneVariant(@NotNull String id, @NotNull String translationKey, float hardness, float resistance) {
            this.id = id;
            this.translationKey = translationKey;
            this.hardness = hardness;
            this.resistance = resistance;
        }
    }
}
