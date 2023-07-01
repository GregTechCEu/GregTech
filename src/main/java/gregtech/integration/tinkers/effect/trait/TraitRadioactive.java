package gregtech.integration.tinkers.effect.trait;

import gregtech.api.GTValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

@SuppressWarnings("ConstantConditions")
public class TraitRadioactive extends AbstractTrait {

    public TraitRadioactive() {
        super("gt_radioactive", 0x73FF7C);
    }

    @Override
    public void afterBlockBreak(ItemStack tool, World world, IBlockState state, BlockPos pos, EntityLivingBase player, boolean wasEffective) {
        if (!world.isRemote) {
            if (GTValues.RNG.nextFloat() < 0.05F)  player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 101, 1));
            if (GTValues.RNG.nextFloat() < 0.01F) player.addPotionEffect(new PotionEffect(MobEffects.POISON, 21, 1));
        }
    }

    @Override
    public void afterHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damageDealt, boolean wasCritical, boolean wasHit) {
        if (wasHit && !player.world.isRemote) {
            if (GTValues.RNG.nextFloat() < 0.05F) player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 101, 1));
            if (GTValues.RNG.nextFloat() < 0.01F) player.addPotionEffect(new PotionEffect(MobEffects.POISON, 21, 1));

            if (target.isEntityAlive()) {
                if (GTValues.RNG.nextFloat() < 0.1F)  target.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 101, 1));
                if (GTValues.RNG.nextFloat() < 0.02F) target.addPotionEffect(new PotionEffect(MobEffects.POISON, 21, 1));
            }
        }
    }
}
