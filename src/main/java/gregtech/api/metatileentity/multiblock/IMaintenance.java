package gregtech.api.metatileentity.multiblock;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maintenance is stored as a byte with the 2 most significant bits ignored. 1 = no issue, 0 = need maintenance. <br>
 * Example: 0b000111 = needs a hard hammer, wire cutter, and a crowbar for maintenance.
 */
public interface IMaintenance {

    /**
     * Immutable mapping between maintenance byte indexes and the tool used to fix said issue.
     */
    Int2ObjectMap<String> maintenance2tool = Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(
            new int[] { 0, 1, 2, 3, 4, 5 },
            new String[] { ToolClasses.WRENCH,
                    ToolClasses.SCREWDRIVER,
                    ToolClasses.SOFT_MALLET,
                    ToolClasses.HARD_HAMMER,
                    ToolClasses.WIRE_CUTTER,
                    ToolClasses.CROWBAR }));

    /**
     * Fill an array with the required tools to maintain a machine
     * 
     * @param problems    the maintenance byte to be checked
     * @param toolClasses a string array to be filled
     * @return if any tools for the maintenance problems were added to the array
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean getToolsForMaintenance(byte problems, @Nullable String @NotNull [] toolClasses) {
        if (problems == 0b111111) return false;

        boolean added = false;
        for (int index = 0; index < 6; index++) {
            if (((problems >> index) & 1) == 0) {
                toolClasses[index] = maintenance2tool.get(index);
                added = true;
            }
        }

        return added;
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
