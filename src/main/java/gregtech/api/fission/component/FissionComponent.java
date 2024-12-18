package gregtech.api.fission.component;

import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FissionComponent {

    /**
     * @param amount the amount to reduce by
     */
    void reduceDurability(int amount);

    /**
     * @return the component's durability
     */
    int durability();

    /**
     * Process neutrons as they move down a path in the reactor
     *
     * @param walker         the walker which triggered the path processing
     * @param neutronData    a list of all the components neutrons interact with
     * @param reactivityData a list of all the components modifying reactivity
     * @param source         the source emitter
     * @param direction      the direction neutrons are travelling in
     * @param r              the row of this component
     * @param c              the column of this component
     * @param neutrons       the neutrons travelling the path
     */
    void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                            @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                            @NotNull ComponentDirection direction, int r, int c, float neutrons);

    BlockPos getPos();
}
