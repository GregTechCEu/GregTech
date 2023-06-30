package gregtech.api.metatileentity.multiblock;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public interface IMaintenance {

    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void causeMaintenanceProblems();

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();

    default SoundEvent getBreakdownSound() {
        return SoundEvents.ENTITY_ITEM_BREAK;
    }
}
