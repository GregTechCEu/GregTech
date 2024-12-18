package gregtech.api.fission.reactor;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ReactorPathWalker {

    /**
     * Process neutrons as they move down a path in the reactor
     *
     * @param neutronData    a list of all the components neutrons interact with
     * @param reactivityData a list of all the components modifying reactivity
     * @param source         the source emitter
     * @param direction      the direction neutrons are travelling in
     * @param startR         the starting row
     * @param startC         the starting column
     * @param neutrons       the neutrons travelling the path
     */
    void walkPath(@NotNull List<NeutronPathData> neutronData, @NotNull List<ReactivityPathData> reactivityData,
                  @NotNull FissionComponent source, @NotNull ComponentDirection direction, int startR, int startC,
                  float neutrons);
}
