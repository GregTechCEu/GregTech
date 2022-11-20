package gregtech.core.advancement;

import gregtech.api.advancement.IAdvancementTrigger;
import gregtech.core.advancement.triggers.BasicTrigger;
import gregtech.core.advancement.triggers.TriggerDeath;

public class AdvancementTriggers {

    public static final IAdvancementTrigger<?> ROTOR_HOLDER_DEATH = new TriggerDeath("rotor_holder_death");
    public static final IAdvancementTrigger<?> ELECTROCUTION_DEATH = new TriggerDeath("electrocution_death");
    public static final IAdvancementTrigger<?> STEAM_VENT_DEATH = new TriggerDeath("steam_vent_death");
    public static final IAdvancementTrigger<?> HEAT_DEATH = new TriggerDeath("heat_death");
    public static final IAdvancementTrigger<?> COLD_DEATH = new TriggerDeath("cold_death");
    public static final IAdvancementTrigger<?> CHEMICAL_DEATH = new TriggerDeath("chemical_death");
    public static final IAdvancementTrigger<?> FIRST_COVER_PLACE = new BasicTrigger("first_cover_place"); // does not work

    // TODO Not Yet Implemented
    public static final IAdvancementTrigger<?> MACHINE_EXPLOSION = new BasicTrigger("machine_explosion");
    public static final IAdvancementTrigger<?> CABLE_BURN = new BasicTrigger("cable_burn");
    public static final IAdvancementTrigger<?> WASH_DUST_CAULDRON = new BasicTrigger("wash_dust_cauldron");

    public static final IAdvancementTrigger<?>[] GT_TRIGGERS = new IAdvancementTrigger[] {
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
