package gregtech.api.recipes.logic;

import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GuardedData;
import gregtech.common.ConfigHolder;

import java.util.Map;

public abstract class RecipeLogicOverclock {

    public static final MapKey.IntKey MACHINE_TIER_KEY = new MapKey.IntKey();
    public static final MapKey.IntKey COIL_TEMP_KEY = new MapKey.IntKey();

    public static final MapKey.IntKey OC_DURATION_KEY = new MapKey.IntKey();
    public static final MapKey.LongKey OC_VOLTAGE_KEY = new MapKey.LongKey();

    public static void loadMachineTier(GuardedData<Map<MapKey<?>, Object>> data, int machineTier) {
        MACHINE_TIER_KEY.put(data.getTransientData(), machineTier);
    }

    public static void overclockStandard(GuardedData<Map<MapKey<?>, Object>> data) {
        overclockInternal(data, RecipeLogicConstants.OVERCLOCK_SPEED_FACTOR, RecipeLogicConstants.OVERCLOCK_VOLTAGE_FACTOR);
    }

    public static void overclockPerfect(GuardedData<Map<MapKey<?>, Object>> data) {
        overclockInternal(data, RecipeLogicConstants.PERFECT_OVERCLOCK_SPEED_FACTOR, RecipeLogicConstants.OVERCLOCK_VOLTAGE_FACTOR);
    }

    public static void overclockFusion(GuardedData<Map<MapKey<?>, Object>> data) {
        overclockInternal(data, RecipeLogicConstants.FUSION_OVERCLOCK_SPEED_FACTOR, RecipeLogicConstants.FUSION_OVERCLOCK_VOLTAGE_FACTOR);
    }

    protected static void overclockInternal(GuardedData<Map<MapKey<?>, Object>> data, double d, double v) {
        RecipeView view = RecipeLogicMatch.RECIPE_VIEW_KEY.get(data.getTransientData());
        if (view == null) {
            RecipeLogicCore.stateError("Attempted to perform overclocking logic without a loaded recipe view.");
            return;
        }
        int m_tier = MACHINE_TIER_KEY.getInt(data.getTransientData());
        int r_tier = GTUtility.getOCTierByVoltage(view.getRecipe().getEUt());
        if (m_tier <= r_tier) return;
        int oc = m_tier - r_tier + 1;
        long resultV;
        long allowedV = RecipeLogicMatch.MATCH_VOLTAGE_KEY.getLong(data.getTransientData());
        do {
            oc--;
            resultV = (long) (view.getActualVoltage() * Math.pow(v, oc));
        } while (resultV > allowedV && oc > 0);
        OC_DURATION_KEY.putInt(data.getTransientData(), (int) (view.getActualDuration() / Math.pow(d, oc)));
        OC_VOLTAGE_KEY.putLong(data.getTransientData(), resultV);
    }

    public static void loadCoilTemperature(GuardedData<Map<MapKey<?>, Object>> data, int temperature) {
        COIL_TEMP_KEY.put(data.getTransientData(), temperature);
    }

    public static void overclockCoil(GuardedData<Map<MapKey<?>, Object>> data) {
        RecipeView view = RecipeLogicMatch.RECIPE_VIEW_KEY.get(data.getTransientData());
        if (view == null) {
            RecipeLogicCore.stateError("Attempted to perform overclocking logic without a loaded recipe view.");
            return;
        }
        int tempDiff = COIL_TEMP_KEY.getInt(data.getTransientData()) - view.getRecipe().getProperty(TemperatureProperty.getInstance(), 0);
        if (tempDiff <= 0) return;
        int vc = tempDiff / RecipeLogicConstants.COIL_VOLTAGE_DISCOUNT_TEMPERATURE;
        int pc = tempDiff / RecipeLogicConstants.COIL_PERFECT_OVERCLOCK_TEMPERATURE;
        double factorVoltage = Math.pow(RecipeLogicConstants.COIL_VOLTAGE_DISCOUNT_FACTOR, vc);
        double factorDuration = 1;
        int m_tier = MACHINE_TIER_KEY.getInt(data.getTransientData());
        int r_tier = GTUtility.getOCTierByVoltage(view.getRecipe().getEUt());
        if (m_tier > r_tier) {
            double originalFactor = factorVoltage;
            long allowedV = RecipeLogicMatch.MATCH_VOLTAGE_KEY.getLong(data.getTransientData());
            int oc = 1 + m_tier - r_tier;
            do {
                oc--;
                factorVoltage = originalFactor * Math.pow(RecipeLogicConstants.OVERCLOCK_VOLTAGE_FACTOR, oc);
            } while (view.getActualVoltage() * factorVoltage > allowedV);
            pc = Math.min(pc, oc);
            factorDuration *= Math.pow(RecipeLogicConstants.PERFECT_OVERCLOCK_SPEED_FACTOR, pc);
            factorDuration *= Math.pow(RecipeLogicConstants.OVERCLOCK_SPEED_FACTOR, m_tier - r_tier - pc);
        }
        OC_DURATION_KEY.putInt(data.getTransientData(), (int) (view.getActualDuration() / factorDuration));
        OC_VOLTAGE_KEY.putLong(data.getTransientData(), (long) (view.getActualVoltage() * factorVoltage));
    }
}
