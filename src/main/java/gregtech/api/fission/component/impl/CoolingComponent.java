package gregtech.api.fission.component.impl;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.impl.data.CoolantData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.ReactionSite;

import gregtech.api.fission.reactor.pathdata.NeutronPathData;

import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class CoolingComponent implements CoolantChannel {

    private CoolantData data;

    protected CoolingComponent() {}

    public void init(@NotNull CoolantData data) {
        this.data = data;
    }

    @Override
    public void applyCooling(@NotNull ReactionSite site) {
        IFluidHandler cold = coldCoolantHandler();
        IFluidHandler hot = hotCoolantHandler();

        FluidStack drained = cold.drain(Integer.MAX_VALUE, false);
        if (drained == null || drained.getFluid() != data.coldCoolant.getFluid()) {
            return;
        }

        int availableCoolant = drained.amount;
        if (availableCoolant == 0) {
            return;
        }

        int hotCoolantSpace = hot.fill(new FluidStack(data.hotCoolant, Integer.MAX_VALUE), false);
        int usableCoolant = Math.min(availableCoolant, hotCoolantSpace / data.hotPerColdCoolant);
        assert usableCoolant <= availableCoolant;

        if (usableCoolant == 0) {
            return;
        }

        drained = cold.drain(usableCoolant, true);
        if (drained == null) {
            return;
        }

        hot.fill(new FluidStack(data.hotCoolant, usableCoolant * data.hotPerColdCoolant), true);

        float heatToRemove = data.heatPerCoolant * usableCoolant;
        site.removeHeat(heatToRemove);
    }

    @Override
    public float coolantHeat() {
        return data.coldCoolantHeat;
    }

    @Override
    public boolean reduceDurability(int amount) {
        return false;
    }

    @Override
    public int durability() {
        return 0;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, int neutrons) {
        // coolant is fully transparent, so neutrons keep going in that direction
        walker.walkPath(neutronData, reactivityData, source, direction, r, c, neutrons);
        if (data.reactivity != 0) {
            reactivityData.add(new ReactivityPathData(this, data.reactivity));
        }
    }

    protected abstract @NotNull IFluidHandler coldCoolantHandler();
    protected abstract @NotNull IFluidHandler hotCoolantHandler();
}
