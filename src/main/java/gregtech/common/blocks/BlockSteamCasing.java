package gregtech.common.blocks;

import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(SteamCasingType.BRONZE_HULL));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        if (getState(stack).ordinal() == 4) {
            super.addInformation(stack, player, tooltip, advanced);
        } else if (getState(stack).ordinal() < 2) {
            tooltip.add(LocalizationUtils.format("tile.steam_casing.bronze.tooltip"));
        } else {
            tooltip.add(LocalizationUtils.format("tile.steam_casing.steel.tooltip"));
        }
    }

    public enum SteamCasingType implements IStringSerializable {

        BRONZE_HULL("bronze_hull"),
        BRONZE_BRICKS_HULL("bronze_bricks_hull"),
        STEEL_HULL("steel_hull"),
        STEEL_BRICKS_HULL("steel_bricks_hull"),
        PUMP_DECK("pump_deck"),
        WOOD_WALL("wood_wall");

        private final String name;

        SteamCasingType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return name;
        }
    }
}
