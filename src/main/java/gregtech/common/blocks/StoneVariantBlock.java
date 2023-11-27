package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
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

    // shared property instance
    private static final PropertyEnum<StoneType> PROPERTY = PropertyEnum.create("variant", StoneType.class);

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
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        this.VARIANT = PROPERTY;
        this.VALUES = StoneType.values();
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public double getWalkingSpeedBonus() {
        return 1.6D;
    }

    @Override
    public boolean checkApplicableBlocks(@NotNull IBlockState state) {
        return state == getState(StoneType.CONCRETE_DARK) || state == getState(StoneType.CONCRETE_LIGHT);
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

    public enum StoneType implements IStringSerializable {

        BLACK_GRANITE("black_granite", MapColor.BLACK),
        RED_GRANITE("red_granite", MapColor.RED),
        MARBLE("marble", MapColor.QUARTZ),
        BASALT("basalt", MapColor.BLACK_STAINED_HARDENED_CLAY),
        CONCRETE_LIGHT("concrete_light", MapColor.STONE),
        CONCRETE_DARK("concrete_dark", MapColor.STONE);

        private final String name;
        public final MapColor mapColor;

        StoneType(@NotNull String name, @NotNull MapColor mapColor) {
            this.name = name;
            this.mapColor = mapColor;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public OrePrefix getOrePrefix() {
            switch (this) {
                case BLACK_GRANITE:
                case RED_GRANITE:
                case MARBLE:
                case BASALT:
                    return OrePrefix.stone;
                case CONCRETE_LIGHT:
                case CONCRETE_DARK:
                    return OrePrefix.block;
                default:
                    throw new IllegalStateException("Unreachable");
            }
        }

        public Material getMaterial() {
            switch (this) {
                case BLACK_GRANITE:
                    return Materials.GraniteBlack;
                case RED_GRANITE:
                    return Materials.GraniteRed;
                case MARBLE:
                    return Materials.Marble;
                case BASALT:
                    return Materials.Basalt;
                case CONCRETE_LIGHT:
                case CONCRETE_DARK:
                    return Materials.Concrete;
                default:
                    throw new IllegalStateException("Unreachable");
            }
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
