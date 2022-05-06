package gregtech.common.advancement;

import gregtech.api.util.advancement.GTTrigger;
import gregtech.common.advancement.criterion.BasicTrigger;
import gregtech.common.advancement.criterion.TriggerDeath;

public class GTTriggers {

    public static final GTTrigger<?> ROTOR_HOLDER_DEATH = new TriggerDeath("rotor_holder_death");
    public static final GTTrigger<?> ELECTROCUTION_DEATH = new TriggerDeath("electrocution_death");
    public static final GTTrigger<?> STEAM_VENT_DEATH = new TriggerDeath("steam_vent_death");
    public static final GTTrigger<?> HEAT_DEATH = new TriggerDeath("heat_death");
    public static final GTTrigger<?> COLD_DEATH = new TriggerDeath("cold_death");
    public static final GTTrigger<?> CHEMICAL_DEATH = new TriggerDeath("chemical_death");
    public static final GTTrigger<?> FIRST_COVER_PLACE = new BasicTrigger("first_cover_place"); // does not work

    // TODO Not Yet Implemented
    public static final GTTrigger<?> MACHINE_EXPLOSION = new BasicTrigger("machine_explosion");
    public static final GTTrigger<?> CABLE_BURN = new BasicTrigger("cable_burn");
    public static final GTTrigger<?> WASH_DUST_CAULDRON = new BasicTrigger("wash_dust_cauldron");

    public static final GTTrigger<?>[] GT_TRIGGERS = new GTTrigger[] {
            ROTOR_HOLDER_DEATH,
            ELECTROCUTION_DEATH,
            STEAM_VENT_DEATH,
            HEAT_DEATH,
            COLD_DEATH,
            CHEMICAL_DEATH,
            FIRST_COVER_PLACE,
            MACHINE_EXPLOSION,
            CABLE_BURN,
            WASH_DUST_CAULDRON,
    };
}
