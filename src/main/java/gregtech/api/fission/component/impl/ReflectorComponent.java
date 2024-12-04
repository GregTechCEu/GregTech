package gregtech.api.fission.component.impl;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;

import gregtech.api.fission.component.impl.data.ReflectorData;

import gregtech.api.fission.reactor.pathdata.NeutronPathData;

import gregtech.api.fission.reactor.ReactorPathWalker;

import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ReflectorComponent implements FissionComponent {

    private ReflectorData data;
    private int durability;

    /**
     * Initialize this component with new data
     *
     * @param data the data to use
     */
    public void init(@NotNull ReflectorData data) {
        this.data = data;
        this.durability = data.durability;
    }

    @Override
    public boolean reduceDurability(int amount) {
        this.durability -= amount;
        return durability > 0;
    }

    @Override
    public int durability() {
        return durability;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, int neutrons) {
        Set<ComponentDirection> nextDirections = data.directions.get(direction.opposite());
        if (nextDirections == null || nextDirections.isEmpty()) {
            // neutron hits an internal wall, is thrown away
            return;
        }

        neutronData.add(new NeutronPathData(this, direction, neutrons));
        for (ComponentDirection next : nextDirections) {
            int nextNeutrons = redirectNeutrons(neutrons);
            // neutron is redirected to new directions
            walker.walkPath(neutronData, reactivityData, source, next, r, c, nextNeutrons);
        }
    }

    private int redirectNeutrons(int amount) {
        if (data.isSplitter()) {
            return amount / data.outputs.size();
        }
        return amount;
    }
}
