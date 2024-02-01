package gregtech.api.unification.material.properties;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class RotorProperty implements IMaterialProperty {

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    private float speed;

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    private float damage;

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    private int durability;

    public RotorProperty() {/**/}

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public RotorProperty(float speed, float damage, int durability) {
        this.speed = speed;
        this.damage = damage;
        this.durability = durability;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public float getSpeed() {
        return speed;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void setSpeed(float speed) {
        if (speed <= 0) throw new IllegalArgumentException("Rotor Speed must be greater than zero!");
        this.speed = speed;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public float getDamage() {
        return damage;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void setDamage(float damage) {
        if (damage <= 0) throw new IllegalArgumentException("Rotor Attack Damage must be greater than zero!");
        this.damage = damage;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public int getDurability() {
        return durability;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void setDurability(int durability) {
        if (durability <= 0) throw new IllegalArgumentException("Rotor Durability must be greater than zero!");
        this.durability = durability;
    }

    @Override
    public void verifyProperty(@NotNull MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    public static class Builder {

        @Deprecated
        private float speed, damage;
        @Deprecated
        private int durability;

        public Builder() {}

        /** Convenience method for transitioning old turbines to new */
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
        public Builder legacyStats(float speed, float damage, int durability) {
            this.speed = speed;
            this.damage = damage;
            this.durability = durability;
            return this;
        }

        public Builder stats() {
            return this; // todo
        }

        // todo maybe break this up to be more expandable?
        public Builder multipliers(float steamMult, float gasMult, float plasmaMult) {
            return this; // todo
        }

        public RotorProperty build() {
            return new RotorProperty(); // todo
        }
    }
}
