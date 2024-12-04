package gregtech.api.fission.component.impl;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EmptyComponent implements FissionComponent {

    @Override
    public boolean reduceDurability(int amount) {
        return false;
    }

    @Override
    public int durability() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, int neutrons) {
        // neutron hits an internal wall, is thrown away
    }
}
