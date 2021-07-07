package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HermeticCasings extends VariantBlock<HermeticCasings.HermeticCasingsType> {

    public HermeticCasings() {
        super(Material.IRON);
        setTranslationKey("hermetic_casing");
        setHardness(2.0f);
        setResistance(8.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(HermeticCasingsType.Hermetic_casing_ulv));
    }
    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }
    public enum HermeticCasingsType implements IStringSerializable {

        Hermetic_casing_ulv("hermetic_casing_ulv"),//ULV
        HermeticCasing_lv("hermetic_casing_lv"),//LV
        HermeticCasing_mv("hermetic_casing_mv"),//MV
        HermeticCasing_hv("hermetic_casing_hv"),//HV
        HermeticCasing_ev("hermetic_casing_ev"),//EV
        HermeticCasing_iv("hermetic_casing_iv"),//IV
        HermeticCasing_luv("hermetic_casing_luv"),//LUV
        HermeticCasing_zpm("hermetic_casing_zpm"),//ZPM
        HermeticCasing_uv("hermetic_casing_uv");//UV


        private final String name;

        HermeticCasingsType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

    }
}

