package gregtech.api.metatileentity.multiblock;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

public interface IMaintenance {

    Int2ObjectMap<String> maintenance2tool = Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(
            new int[] { 0, 1, 2, 3, 4, 5 },
            new String[] { ToolClasses.WRENCH,
                    ToolClasses.SCREWDRIVER,
                    ToolClasses.SOFT_MALLET,
                    ToolClasses.HARD_HAMMER,
                    ToolClasses.WIRE_CUTTER,
                    ToolClasses.CROWBAR }));

    static boolean getToolsForMaintenance(byte problems, String[] toolClasses) {
        if (problems == 0b000000) return false;
    }

    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void fixAllMaintenance();

    void causeMaintenanceProblems();

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();

    default SoundEvent getBreakdownSound() {
        return SoundEvents.ENTITY_ITEM_BREAK;
    }
}
