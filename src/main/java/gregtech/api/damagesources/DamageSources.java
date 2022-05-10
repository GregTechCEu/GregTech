package gregtech.api.damagesources;

import net.minecraft.util.DamageSource;

public class DamageSources {

    private static final DamageSource EXPLOSION = new DamageSource("explosion").setExplosion();
    private static final DamageSource HEAT = new DamageSource("heat").setFireDamage();
    private static final DamageSource FROST = new DamageSource("frost");
    private static final DamageSource CHEMICAL = new DamageSource("chemical").setDamageBypassesArmor();
    private static final DamageSource ELECTRIC = new DamageSource("electric");
    private static final DamageSource RADIATION = new DamageSource("radiation").setDamageBypassesArmor();
    private static final DamageSource TURBINE = new DamageSource("turbine");

    public static DamageSource getExplodingDamage() {
        return EXPLOSION;
    }

    public static DamageSource getHeatDamage() {
        return HEAT;
    }

    public static DamageSource getFrostDamage() {
        return FROST;
    }

    public static DamageSource getChemicalDamage() {
        return CHEMICAL;
    }

    public static DamageSource getElectricDamage() {
        return ELECTRIC;
    }

    public static DamageSource getRadioactiveDamage() {
        return RADIATION;
    }

    public static DamageSource getTurbineDamage() {
        return TURBINE;
    }
}
