package gregtech.api.fission.reactor;

import gregtech.api.fission.component.ControlRod;
import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.ReactiveComponent;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReactionSite {

    private final List<NeutronPathData> neutronsPerCycle = new ArrayList<>();
    private final List<ReactivityPathData> reactivity = new ArrayList<>();
    private final ReactiveComponent reactiveComponent;
    private final List<CoolantChannel> coolantChannels;
    private final List<ControlRod> controlRods;

    private final float minHeat;

    private float neutronsPerCycleCached;
    private float reactivityCached;

    private float targetHeat;
    private float heat;

    public ReactionSite(@NotNull ReactiveComponent reactiveComponent, @NotNull List<CoolantChannel> coolantChannels,
                        @NotNull List<ControlRod> controlRods) {
        this.reactiveComponent = reactiveComponent;
        this.coolantChannels = coolantChannels;
        this.controlRods = controlRods;
        float max = 0;
        for (CoolantChannel coolantChannel : coolantChannels) {
            if (coolantChannel != null && coolantChannel.coolantHeat() > max) {
                max = coolantChannel.coolantHeat();
            }
        }
        this.minHeat = max;
        this.heat = minHeat;
    }

    public void run() {
        // TODO simulation for player display
        // set some target heat value
        // compute the participating neutrons, react them with a fake reactive component (or simulate bool)
        // multiply by heatPerFission for heat generated, then figure out how much cooling is needed to cool that much
        // heat
        // using all of the coolers at the site
        // this gives the coolant rate per cycle required to maintain the heat target and also allows computing fuel
        // lifespan
        if (reactiveComponent.canReact()) {
            float toReact = computeParticipatingNeutrons();
            if (toReact >= 0.1) {
                // numbers < 0.1 are too small to be meaningful

                float reacted = reactiveComponent.react(toReact);
                if (reacted != 0) {
                    float heat = reacted * reactiveComponent.heatPerFission();
                    for (ControlRod controlRod : controlRods) {
                        heat = controlRod.adjustHeat(heat);
                    }
                    addHeat(heat);
                    // require at least 1 durability to be lost
                    reactiveComponent.reduceDurability(Math.max(1, (int) reacted));
                }

                float delta = toReact - reacted;
                // TODO emit radiation in +Y and -Y based on this delta
            }
        }

        for (CoolantChannel channel : coolantChannels) {
            if (heat <= minHeat) {
                return;
            }
            if (channel == null) {
                continue;
            }
            if (heat <= targetHeat) {
                return;
            }

            removeHeat(channel.applyCooling(heat - targetHeat));
        }
    }

    private float computeParticipatingNeutrons() {
        float neutrons = neutronsPerCycleCached * reactivityCached;
        for (ControlRod rod : controlRods) {
            neutrons *= rod.adjustNeutrons(neutrons);
        }
        for (CoolantChannel channel : coolantChannels) {
            neutrons *= channel.adjustNeutrons(neutrons);
        }
        return neutrons;
        // TODO sort out heat scaling
        // float heatFactor = (neutronsPerCycleCached / reactivityCached) * (heat - minHeat) / 100;
        // return Math.max(0, base - heatFactor);
    }

    public float heat() {
        return heat;
    }

    public void addHeat(float heat) {
        this.heat += heat;
    }

    public void removeHeat(float heat) {
        this.heat = Math.max(minHeat, this.heat - heat);
    }

    public float targetHeat() {
        return targetHeat;
    }

    public void setTargetHeat(float targetHeat) {
        this.targetHeat = targetHeat;
    }

    public void addNeutronsPerCycle(@NotNull NeutronPathData pathData) {
        neutronsPerCycle.add(pathData);
    }

    public void addReactivity(@NotNull ReactivityPathData pathData) {
        reactivity.add(pathData);
    }

    /**
     * Recompute internal cached data
     */
    public void recompute() {
        for (NeutronPathData data : neutronsPerCycle) {
            neutronsPerCycleCached += data.getNeutrons();
        }
        for (ReactivityPathData data : reactivity) {
            reactivityCached += data.getReactivity();
        }
    }

    public boolean hasFuel() {
        return this.reactiveComponent.durability() > 0;
    }

    public float lifespan() {
        return reactiveComponent.durabilityPercent();
    }
}
