package gregtech.api.recipes.logic.statemachine;

import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.nbt.NBTTagCompound;

import com.github.bsideup.jabel.Desugar;

import java.util.Map;
import java.util.function.Supplier;

public class RecipeMaintenanceOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_KEY = "MaintenanceInfo";

    protected final Supplier<MaintenanceValues> maintenance;
    protected final String key;

    public RecipeMaintenanceOperator(Supplier<MaintenanceValues> maintenance) {
        this.maintenance = maintenance;
        this.key = STANDARD_KEY;
    }

    public RecipeMaintenanceOperator(Supplier<MaintenanceValues> maintenance, String key) {
        this.maintenance = maintenance;
        this.key = key;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        transientData.put(key, maintenance.get());
    }

    @Desugar
    public record MaintenanceValues(int count, double durationBonus) {}
}
