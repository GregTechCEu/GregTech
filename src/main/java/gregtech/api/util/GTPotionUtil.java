package gregtech.api.util;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class GTPotionUtil {

    public static PotionEffect copyPotionEffect(PotionEffect sample) {
        PotionEffect potionEffect = new PotionEffect(sample.getPotion(), sample.getDuration(), sample.getAmplifier(), sample.getIsAmbient(), sample.doesShowParticles());
        potionEffect.setCurativeItems(sample.getCurativeItems());
        return potionEffect;
    }


    public static class ChancedPotionEffect {

        public final PotionEffect effect;

        /**
         * 100 equals 100%
         */
        public final int chance;

        public ChancedPotionEffect(Potion potion, int duration, int amplifier, int chance) {
            this.effect = new PotionEffect(potion, duration, amplifier);
            this.chance = chance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChancedPotionEffect that = (ChancedPotionEffect) o;

            if (chance != that.chance) return false;
            return effect.equals(that.effect);
        }

        @Override
        public int hashCode() {
            int result = effect.hashCode();
            result = 31 * result + chance;
            return result;
        }

    }
}
