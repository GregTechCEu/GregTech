package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
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
public class BlockCleanroomCasing extends VariantBlock<BlockCleanroomCasing.CasingType> implements IStateHarvestLevel {

    public BlockCleanroomCasing() {
        super(Material.IRON);
        setTranslationKey("cleanroom_casing");
        setHardness(2.0f);
        setResistance(8.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(CasingType.PLASCRETE));
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable {

        PLASCRETE("plascrete"),
        FILTER_CASING("filter_casing"),
        FILTER_CASING_STERILE("filter_casing_sterile");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Nonnull
        @Override
        public String toString() {
            return getName();
        }
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return state == getState(CasingType.PLASCRETE) ? 2 : 1;
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state) {
        return state == getState(CasingType.PLASCRETE) ? "pickaxe" : "wrench";
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (stack.isItemEqual(getItemVariant(CasingType.FILTER_CASING))) tooltip.add(I18n.format("tile.cleanroom_casing.filter.tooltip"));
        if (stack.isItemEqual(getItemVariant(CasingType.FILTER_CASING_STERILE))) tooltip.add(I18n.format("tile.cleanroom_casing.filter_sterile.tooltip"));
    }
}
