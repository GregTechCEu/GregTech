package gregtech.core.advancement;

import gregtech.api.advancement.IAdvancementTrigger;
import gregtech.core.advancement.criterion.BasicCriterion;
import gregtech.core.advancement.criterion.DeathCriterion;

import static gregtech.api.GregTechAPI.advancementManager;

public class AdvancementTriggers {

    public static IAdvancementTrigger<?> ROTOR_HOLDER_DEATH;
    public static IAdvancementTrigger<?> ELECTROCUTION_DEATH;
    public static IAdvancementTrigger<?> STEAM_VENT_DEATH;
    public static IAdvancementTrigger<?> HEAT_DEATH;
    public static IAdvancementTrigger<?> COLD_DEATH;
    public static IAdvancementTrigger<?> CHEMICAL_DEATH;
    public static IAdvancementTrigger<?> FIRST_COVER_PLACE; // does not work

    // TODO Not Yet Implemented
    public static IAdvancementTrigger<?> MACHINE_EXPLOSION;
    public static IAdvancementTrigger<?> CABLE_BURN;
    public static IAdvancementTrigger<?> WASH_DUST_CAULDRON;

    public static void register() {
        ROTOR_HOLDER_DEATH = advancementManager.registerTrigger("rotor_holder_death", new DeathCriterion());
        ELECTROCUTION_DEATH = advancementManager.registerTrigger("electrocution_death", new DeathCriterion());
        STEAM_VENT_DEATH = advancementManager.registerTrigger("steam_vent_death", new DeathCriterion());
        HEAT_DEATH = advancementManager.registerTrigger("heat_death", new DeathCriterion());
        COLD_DEATH = advancementManager.registerTrigger("cold_death", new DeathCriterion());
        CHEMICAL_DEATH = advancementManager.registerTrigger("chemical_death", new DeathCriterion());
        FIRST_COVER_PLACE = advancementManager.registerTrigger("first_cover_place", new BasicCriterion());
        MACHINE_EXPLOSION = advancementManager.registerTrigger("machine_explosion", new BasicCriterion());
        CABLE_BURN = advancementManager.registerTrigger("cable_burn", new BasicCriterion());
        WASH_DUST_CAULDRON = advancementManager.registerTrigger("wash_dust_cauldron", new BasicCriterion());
    }
}
