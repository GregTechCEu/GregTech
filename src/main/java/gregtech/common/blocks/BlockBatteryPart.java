package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.multiblock.IBatteryData;

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

public class BlockBatteryPart extends VariantBlock<BlockBatteryPart.BatteryPartType> {

    public BlockBatteryPart() {
        super(Material.IRON);
        setTranslationKey("battery_block");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 3); // Diamond level, can be mined by a steel wrench or better
        setDefaultState(getState(BatteryPartType.EMPTY_TIER_I));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType placementType) {
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);

        BatteryPartType batteryType = getState(stack);
        if (batteryType.getCapacity() != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", batteryType.getCapacity()));
        } else {
            tooltip.add(I18n.format("tile.battery_block.tooltip_empty"));
        }
    }

    public enum BatteryPartType implements IStringSerializable, IBatteryData {

        EMPTY_TIER_I,
        LAPOTRONIC_EV(GTValues.EV, 25_000_000L * 6),      // Lapotron Crystal * 6
        LAPOTRONIC_IV(GTValues.IV, 250_000_000L * 6),     // Lapotronic Orb * 6

        EMPTY_TIER_II,
        LAPOTRONIC_LuV(GTValues.LuV, 1_000_000_000L * 6), // Lapotronic Orb Cluster * 6
        LAPOTRONIC_ZPM(GTValues.ZPM, 4_000_000_000L * 6), // Energy Orb * 6

        EMPTY_TIER_III,
        LAPOTRONIC_UV(GTValues.UV, 16_000_000_000L * 6),  // Energy Cluster * 6
        ULTIMATE_UHV(GTValues.UHV, Long.MAX_VALUE),       // Ultimate Battery
        ;

        private final int tier;
        private final long capacity;

        BatteryPartType() {
            this.tier = -1;
            this.capacity = 0;
        }

        BatteryPartType(int tier, long capacity) {
            this.tier = tier;
            this.capacity = capacity;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        // must be separately named because of reobf issue
        @NotNull
        @Override
        public String getBatteryName() {
            return name().toLowerCase();
        }

        @NotNull
        @Override
        public String getName() {
            return getBatteryName();
        }
    }
}
