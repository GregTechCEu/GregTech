package gregtech.api.fission.component.impl;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.ReactiveComponent;
import gregtech.api.fission.component.impl.data.ModeratorData;

import gregtech.api.fission.reactor.pathdata.NeutronPathData;

import gregtech.api.fission.reactor.ReactorPathWalker;

import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModeratorComponent implements FissionComponent {

    private ModeratorData data;
    private int durability;

    /**
     * Initialize this component with new data
     *
     * @param data the data to use
     */
    public void init(@NotNull ModeratorData data) {
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
        if (source instanceof ReactiveComponent reactor) {
            // neutron hits a moderator, so the neutron bounces back along the path and hits the source
            // only if the source reacts with neutrons
            neutronData.add(new NeutronPathData(this, direction, neutrons));
            neutronData.add(new NeutronPathData(reactor, direction, neutrons));
        }
        if (data.reactivity != 0) {
            reactivityData.add(new ReactivityPathData(this, data.reactivity));
        }
    }
}
