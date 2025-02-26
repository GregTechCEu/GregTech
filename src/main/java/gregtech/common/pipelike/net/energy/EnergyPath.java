package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.path.NetPath;

import org.jetbrains.annotations.NotNull;

public interface EnergyPath extends NetPath {

    /**
     * Does the calculations to traverse the path.
     * 
     * @param voltage  the input voltage.
     * @param amperage the input amperage.
     * @return the flow report for the traversal.
     */
    @NotNull
    PathFlowReport traverse(long voltage, long amperage);

    interface PathFlowReport {

        /**
         * @return the total voltage that was allowed through the path
         */
        long voltageOut();

        /**
         * @return the total amperage that was allowed through the path
         */
        long amperageOut();

        /**
         * @return the total EU that was allowed through the path
         */
        default long euOut() {
            return voltageOut() * amperageOut();
        }

        /**
         * Called when this flow report should stop being simulated;
         * e.g. flow should be reported and heating should occur.
         */
        void report();
    }
}
