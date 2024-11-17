package gregtech.common.blocks;

import gregtech.api.block.ICleanroomFilter;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.multiblock.CleanroomType;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public enum CasingType implements IStringSerializable, ICleanroomFilter {

        PLASCRETE("plascrete", null),
        FILTER_CASING("filter_casing", CleanroomType.CLEANROOM),
        FILTER_CASING_STERILE("filter_casing_sterile", CleanroomType.STERILE_CLEANROOM);

        private final String name;
        private final CleanroomType cleanroomType;

        CasingType(String name, CleanroomType cleanroomType) {
            this.name = name;
            this.cleanroomType = cleanroomType;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }

        @Override
        @Nullable
        public CleanroomType getCleanroomType() {
            return cleanroomType;
        }

        @Override
        public int getTier() {
            return this.ordinal() - 1;
        }
    }

    @Override
    public int getHarvestLevel(@NotNull IBlockState state) {
        return state == getState(CasingType.PLASCRETE) ? 2 : 1;
    }

    @Nullable
    @Override
    public String getHarvestTool(@NotNull IBlockState state) {
        return state == getState(CasingType.PLASCRETE) ? ToolClasses.PICKAXE : ToolClasses.WRENCH;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (stack.isItemEqual(getItemVariant(CasingType.FILTER_CASING)))
            tooltip.add(I18n.format("tile.cleanroom_casing.filter.tooltip"));
        if (stack.isItemEqual(getItemVariant(CasingType.FILTER_CASING_STERILE)))
            tooltip.add(I18n.format("tile.cleanroom_casing.filter_sterile.tooltip"));
    }
}
