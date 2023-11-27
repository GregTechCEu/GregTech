package gregtech.common.blocks;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static gregtech.api.GTValues.VOLTAGE_NAMES;

public class BlockMachineCasing extends VariantBlock<BlockMachineCasing.MachineCasingType> {

    public BlockMachineCasing() {
        super(Material.IRON);
        setTranslationKey("machine_casing");
        setHardness(4.0f);
        setResistance(8.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel(ToolClasses.WRENCH, 2);
        setDefaultState(getState(MachineCasingType.ULV));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        for (MachineCasingType variant : VALUES) {
            if (variant.ordinal() <= MachineCasingType.UHV.ordinal() || GregTechAPI.isHighTier()) {
                list.add(getItemVariant(variant));
            }
        }
    }

    public enum MachineCasingType implements IStringSerializable {

        // Voltage-tiered casings
        ULV(makeName(VOLTAGE_NAMES[GTValues.ULV])),
        LV(makeName(VOLTAGE_NAMES[GTValues.LV])),
        MV(makeName(VOLTAGE_NAMES[GTValues.MV])),
        HV(makeName(VOLTAGE_NAMES[GTValues.HV])),
        EV(makeName(VOLTAGE_NAMES[GTValues.EV])),
        IV(makeName(VOLTAGE_NAMES[GTValues.IV])),
        LuV(makeName(VOLTAGE_NAMES[GTValues.LuV])),
        ZPM(makeName(VOLTAGE_NAMES[GTValues.ZPM])),
        UV(makeName(VOLTAGE_NAMES[GTValues.UV])),
        UHV(makeName(VOLTAGE_NAMES[GTValues.UHV])),
        UEV(makeName(VOLTAGE_NAMES[GTValues.UEV])),
        UIV(makeName(VOLTAGE_NAMES[GTValues.UIV])),
        UXV(makeName(VOLTAGE_NAMES[GTValues.UXV])),
        OpV(makeName(VOLTAGE_NAMES[GTValues.OpV])),
        MAX(makeName(VOLTAGE_NAMES[GTValues.MAX]));

        private final String name;

        MachineCasingType(String name) {
            this.name = name;
        }

        @Override
        @NotNull
        public String getName() {
            return this.name;
        }

        private static String makeName(String voltageName) {
            return voltageName.toLowerCase(Locale.ROOT).replace(' ', '_');
        }
    }
}
