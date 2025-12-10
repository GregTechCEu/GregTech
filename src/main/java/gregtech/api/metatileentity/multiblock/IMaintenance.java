package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.ConfigHolder;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;

public interface IMaintenance {

    /**
     * Mapping between maintenance byte indexes and the tool class used to fix the issue.
     */
    @UnmodifiableView
    Int2ObjectMap<String> maintenance2tool = Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(
            new int[] { 0, 1, 2, 3, 4, 5 },
            new String[] { ToolClasses.WRENCH,
                    ToolClasses.SCREWDRIVER,
                    ToolClasses.SOFT_MALLET,
                    ToolClasses.HARD_HAMMER,
                    ToolClasses.WIRE_CUTTER,
                    ToolClasses.CROWBAR }));

    /**
     * How many unique problems are possible. <br/>
     * When calculating maintenance values, bit indices higher than {@code POSSIBLE_PROBLEMS - 1} are ignored.
     */
    int POSSIBLE_PROBLEMS = maintenance2tool.size();

    /**
     * Represents a maintenance byte where there are no problems, ie {@code 0b111111}.
     */
    byte NO_PROBLEMS = (byte) ((1 << POSSIBLE_PROBLEMS) - 1);

    default Set<Int2ObjectMap.Entry<String>> getToolsForMaintenance() {
        byte problems = getMaintenanceProblems();
        if (problems == NO_PROBLEMS) return Collections.emptySet();

        Set<Int2ObjectMap.Entry<String>> entries = new ObjectArraySet<>(maintenance2tool.int2ObjectEntrySet());
        entries.removeIf(stringEntry -> ((problems >> stringEntry.getIntKey()) & 1) == 1);

        return entries;
    }

    /**
     * Get the current maintenance problems. <br/>
     * Each problem type is a single bit in the returned byte: <br/>
     * - {@code 0}: maintenance needed <br/>
     * - {@code 1}: no maintenance needed <br/>
     * See {@link #maintenance2tool} for the mapping between bit index and tool class.
     */
    byte getMaintenanceProblems();

    /**
     * Set the maintenance problems of this to the given byte.
     */
    void setMaintenance(byte maintenance);

    /**
     * @return how many unique maintenance problems this has.
     */
    default int getNumMaintenanceProblems() {
        if (!hasMaintenanceMechanics()) return 0;
        return POSSIBLE_PROBLEMS - Integer.bitCount(getMaintenanceProblems());
    }

    /**
     * @return if this has any maintenance problems.
     */
    default boolean hasMaintenanceProblems() {
        if (!hasMaintenanceMechanics()) return false;
        return getMaintenanceProblems() < NO_PROBLEMS;
    }

    /**
     * Set a certain problem as fixed or unfixed.
     */
    default void setMaintenanceAt(int index, boolean fixed) {
        if (index >= POSSIBLE_PROBLEMS) return;

        byte maintenance = getMaintenanceProblems();
        if (fixed) {
            maintenance |= (byte) (1 << index);
        } else {
            maintenance &= (byte) ~(1 << index);
        }

        setMaintenance(maintenance);
    }

    /**
     * Set a certain problem as fixed.
     */
    default void setMaintenanceFixed(int index) {
        setMaintenanceAt(index, true);
    }

    /**
     * Fix every maintenance problem this has.
     */
    default void fixAllMaintenance() {
        setMaintenance(NO_PROBLEMS);
    }

    /**
     * Cause a random maintenance problem to need fixing.
     */
    default void causeMaintenanceProblems() {
        setMaintenanceAt(GTValues.RNG.nextInt(POSSIBLE_PROBLEMS), false);
    }

    void storeTaped(boolean isTaped);

    /**
     * If overridden, it is recommended that you only ever return false as to not conflict with the maintenance config.
     */
    default boolean hasMaintenanceMechanics() {
        return ConfigHolder.machines.enableMaintenance;
    }

    default SoundEvent getBreakdownSound() {
        return SoundEvents.ENTITY_ITEM_BREAK;
    }
}
