package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.block.VariantBlock;
import gregtech.api.metatileentity.multiblock.IBatteryDataProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

public class BlockBatteryPart extends VariantBlock<BlockBatteryPart.BatteryPartType> implements IBatteryDataProvider {

    public BlockBatteryPart() {
        super(Material.IRON);
        setTranslationKey("battery_block");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(BatteryPartType.EMPTY));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.SpawnPlacementType placementType) {
        return false;
    }

    @Override
    public IBatteryData getData(IBlockState state) {
        return getState(state);
    }

    public enum BatteryPartType implements IStringSerializable, IBatteryDataProvider.IBatteryData {
        EMPTY(-1, 0),
        LAPOTRONIC_EV(GTValues.EV, 25_000_000L * 6),      // Lapotron Crystal * 6
        LAPOTRONIC_IV(GTValues.IV, 250_000_000L * 6),     // Lapotronic Orb * 6
        LAPOTRONIC_LuV(GTValues.LuV, 1_000_000_000L * 6), // Lapotronic Orb Cluster * 6
        LAPOTRONIC_ZPM(GTValues.ZPM, 4_000_000_000L * 6), // Energy Orb * 6
        LAPOTRONIC_UV(GTValues.UV, 16_000_000_000L * 6),  // Energy Cluster * 6
        ULTIMATE_UHV(GTValues.UHV, Long.MAX_VALUE),       // Ultimate Battery
        ;

        private final int tier;
        private final long capacity;

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

        @NotNull
        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }
}
