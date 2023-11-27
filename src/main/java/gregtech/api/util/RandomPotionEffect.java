package gregtech.api.util;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class RandomPotionEffect {

    public final PotionEffect effect;

    /**
     * 100 equals 100%
     */
    public final int chance;

    public RandomPotionEffect(Potion potion, int duration, int amplifier, int chance) {
        this.effect = new PotionEffect(potion, duration, amplifier);
        this.chance = chance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RandomPotionEffect that = (RandomPotionEffect) o;

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
