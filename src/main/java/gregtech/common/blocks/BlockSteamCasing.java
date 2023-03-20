package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockSteamCasing extends VariantBlock<BlockSteamCasing.SteamCasingType> {

    public BlockSteamCasing() {
        super(Material.IRON);
        setTranslationKey("steam_casing");
        setHardness(4.0f);
        setResistance(8.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(SteamCasingType.BRONZE_HULL));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        int ordinal = getState(stack).ordinal();
        if (ordinal < 2) {
            tooltip.add(I18n.format("tile.steam_casing.bronze.tooltip"));
        } else if (ordinal < 4) {
            tooltip.add(I18n.format("tile.steam_casing.steel.tooltip"));
        } else {
            super.addInformation(stack, player, tooltip, advanced);
        }
    }

    public enum SteamCasingType implements IStringSerializable, IStateHarvestLevel {

        BRONZE_HULL("bronze_hull", 1),
        BRONZE_BRICKS_HULL("bronze_bricks_hull", 1),
        STEEL_HULL("steel_hull", 2),
        STEEL_BRICKS_HULL("steel_bricks_hull", 2),
        PUMP_DECK("pump_deck", 1),
        WOOD_WALL("wood_wall", 0);

        private final String name;
        private final int harvestLevel;

        SteamCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        @Nonnull
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return ToolClasses.WRENCH;
        }
    }
}
