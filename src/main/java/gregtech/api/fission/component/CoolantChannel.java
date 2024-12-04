package gregtech.api.fission.component;

import gregtech.api.fission.reactor.ReactionSite;

import org.jetbrains.annotations.NotNull;

public interface CoolantChannel extends FissionComponent {

    /**
     * @param site the site to apply cooling to
     */
    void applyCooling(@NotNull ReactionSite site);

    /**
     * @return the passive heat from the coolant
     */
    float coolantHeat();
}
