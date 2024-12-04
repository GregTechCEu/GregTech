package gregtech.api.fission.component.impl;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.NeutronEmitter;
import gregtech.api.fission.component.ReactiveComponent;
import gregtech.api.fission.component.impl.data.FuelData;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.ReactorPathWalker;

import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuelComponent implements NeutronEmitter, ReactiveComponent {

    private FuelData data;
    private int durability;

    /**
     * Initialize this component with new data
     *
     * @param data the data to use
     */
    public void init(@NotNull FuelData data) {
        this.data = data;
        this.durability = data.durability;
    }

    @Override
    public int generateNeutrons() {
        return data.emission;
    }

    @Override
    public int react(int amount) {
        return Math.min(amount, durability);
    }

    @Override
    public float heatPerFission() {
        return data.heatPerFission;
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
        // neutron hits a reactive fuel
        neutronData.add(new NeutronPathData(this, direction, neutrons));
    }
}
