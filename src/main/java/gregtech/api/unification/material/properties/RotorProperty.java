package gregtech.api.unification.material.properties;

import org.jetbrains.annotations.NotNull;

public class RotorProperty implements IMaterialProperty {

    /**
     * Speed of rotors made from this Material.
     * <p>
     * Default:
     */
    private float speed;

    /**
     * Attack damage of rotors made from this Material
     * <p>
     * Default:
     */
    private float damage;

    /**
     * Durability of rotors made from this Material.
     * <p>
     * Default:
     */
    private int durability;

    public RotorProperty(float speed, float damage, int durability) {
        this.speed = speed;
        this.damage = damage;
        this.durability = durability;
    }

    public float getSpeed() {
        return speed;
    }

    public RotorProperty setSpeed(float speed) {
        if (speed <= 0) throw new IllegalArgumentException("Rotor Speed must be greater than zero!");
        this.speed = speed;
        return this;
    }

    public float getDamage() {
        return damage;
    }

    public RotorProperty setDamage(float damage) {
        if (damage <= 0) throw new IllegalArgumentException("Rotor Attack Damage must be greater than zero!");
        this.damage = damage;
        return this;
    }

    public int getDurability() {
        return durability;
    }

    public RotorProperty setDurability(int durability) {
        if (durability <= 0) throw new IllegalArgumentException("Rotor Durability must be greater than zero!");
        this.durability = durability;
        return this;
    }

    @Override
    public void verifyProperty(@NotNull MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }
}
