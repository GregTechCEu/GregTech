package gregtech.common.blocks;

import gregtech.api.GTValues;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (MachineCasingType variant : VALUES) {
            if (variant.ordinal() <= MachineCasingType.UHV.ordinal() || GTValues.HT) {
                list.add(getItemVariant(variant));
            }
        }
    }

    public enum MachineCasingType implements IStringSerializable {

        //Voltage-tiered casings
        ULV(makeName(GTValues.VOLTAGE_NAMES[0])),
        LV(makeName(GTValues.VOLTAGE_NAMES[1])),
        MV(makeName(GTValues.VOLTAGE_NAMES[2])),
        HV(makeName(GTValues.VOLTAGE_NAMES[3])),
        EV(makeName(GTValues.VOLTAGE_NAMES[4])),
        IV(makeName(GTValues.VOLTAGE_NAMES[5])),
        LuV(makeName(GTValues.VOLTAGE_NAMES[6])),
        ZPM(makeName(GTValues.VOLTAGE_NAMES[7])),
        UV(makeName(GTValues.VOLTAGE_NAMES[8])),
        UHV(makeName(GTValues.VOLTAGE_NAMES[9])),
        UEV(makeName(GTValues.VOLTAGE_NAMES[10])),
        UIV(makeName(GTValues.VOLTAGE_NAMES[11])),
        UXV(makeName(GTValues.VOLTAGE_NAMES[12])),
        OpV(makeName(GTValues.VOLTAGE_NAMES[13])),
        MAX(makeName(GTValues.VOLTAGE_NAMES[14]));

        private final String name;

        MachineCasingType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getName() {
            return this.name;
        }

        private static String makeName(String voltageName) {
            return String.join("_", voltageName.toLowerCase().split(" "));
        }
    }
}
