package gregtech.api.capability;

import gregtech.api.metatileentity.multiblock.IFissionReactor;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IFissionRodPort {

    /**
     * Called during structure formation. The RodType for two opposing rod ports will be compared with
     * REFERENCE EQUIVALENCE {@code ==}
     * to determine if they are compatible. Then that RodType will be assigned to the two ports. If compatibility
     * fails for any pair of rod ports, then structure formation is cancelled.
     */
    @NotNull RodType getRodType();

    BlockPos getPos();

    interface RodType {

        /**
         * Called every reactor tick for all rods.
         * @param reactor the reactor that is ticking.
         * @param port one of the fission rod ports forming a rod of this type.
         * @param opposingPort the other fission rod port forming a rod of this type.
         * @param rodLength the length of the rod formed by the two ports. Counts only the number of zircaloy beams
         *                  between the two ports, not that plus the length of the ports.
         */
        void onReactorTick(@NotNull IFissionReactor reactor, @NotNull IFissionRodPort port, @NotNull IFissionRodPort opposingPort, int rodLength);

        /**
         * Called when reactor stats are computed, to determine if the stats of this rod should be included.
         * Useful for moderators or control rods that consume material to operate, for example.
         * @param reactor the reactor that is ticking.
         * @param port one of the fission rod ports forming a rod of this type.
         * @param opposingPort the other fission rod port forming a rod of this type.
         * @param rodLength the length of the rod formed by the two ports. Counts only the number of zircaloy beams
         *                  between the two ports, not that plus the length of the ports.
         * @see IFissionReactor#recomputeRodStats()
         * @return whether this rod's stats should be included in the final totals.
         */
        default boolean isOperational(@NotNull IFissionReactor reactor, @NotNull IFissionRodPort port, @NotNull IFissionRodPort opposingPort, int rodLength) {
            return true;
        }

        /**
         * Fuel rod count is summed across all rods, then used as the parallel limit of the fission reactor.
         */
        default int getFuelRodCount() {
            return 0;
        }

        /**
         * Fuel rod penalty is summed across all rods and 10 is added,
         * then the bonus effects of moderators and control rods are multiplied by ten and divided by the sum.
         * @see #getModeratorBonus()
         * @see #getControlRodBonus()
         */
        default int getFuelRodPenalty() {
            return 0;
        }

        /**
         * Cooling parallels increase coolant throughput for rods for every 1000K above coolant minimum temperature,
         * in a continuous manner. Should be greater than 0 for cooling rods, and 0 for everything else.
         */
        default int getCoolingParallelsPer1000K() {
            return 0;
        }

        /**
         * Instability is summed across all rods multiplied by their rod length and reduced by 1 for every empty space
         * in the reactor; net positive instability reduces maximum temperature before meltdown.
         */
        default int getInstabilityPerLength() {
            return 0;
        }

        /**
         * Fragility is summed across all rods multiplied by their rod length and reduced by 1 for every empty space
         * in the reactor; net positive fragility directly reduces reaction rate.
         */
        default int getFragilityPerLength() {
            return 0;
        }

        /**
         * Moderator bonus is summed across all rods, divided based on fuel rod penalty,
         * and then is directly added to the reaction rate.
         */
        default double getModeratorBonus() {
            return 0;
        }

        /**
         * Moderator bonus is summed across all rods, divided based on fuel rod penalty,
         * and then is added to the optimal temperature for the reaction.
         * @see #getFuelRodPenalty()
         */
        default int getControlRodBonus() {
            return 0;
        }

        default void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
            if (getFuelRodCount() != 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.fuel.desc", getFuelRodCount(), getFuelRodPenalty() / 10f));
            }
            if (getCoolingParallelsPer1000K() > 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.cooling.desc.1", getCoolingParallelsPer1000K()));
                tooltip.add(I18n.format("gregtech.machine.fission.cooling.desc.2"));
            }
            if (getModeratorBonus() != 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.moderator.desc", getModeratorBonus()));
            }
            if (getControlRodBonus() != 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.control.desc", getControlRodBonus()));
            }
            if (getInstabilityPerLength() != 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.instability", getInstabilityPerLength()));
            }
            if (getFragilityPerLength() != 0) {
                tooltip.add(I18n.format("gregtech.machine.fission.fragility", getFragilityPerLength()));
            }
        }
    }

}
