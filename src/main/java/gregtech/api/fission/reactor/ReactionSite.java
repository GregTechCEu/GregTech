package gregtech.api.fission.reactor;

import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.ReactiveComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactionSite {

    private final ReactiveComponent reactiveComponent;
    private final @Nullable CoolantChannel [] coolantChannels;
    private final float minHeat;

    private int neutronsPerCycle;
    private float reactivity;
    private float heat;

    public ReactionSite(@NotNull ReactiveComponent reactiveComponent,
                        @Nullable CoolantChannel @NotNull [] coolantChannels) {
        this.reactiveComponent = reactiveComponent;
        this.coolantChannels = coolantChannels;
        float min = Float.MAX_VALUE;
        for (CoolantChannel coolantChannel : coolantChannels) {
            if (coolantChannel != null && coolantChannel.coolantHeat() < min) {
                min = coolantChannel.coolantHeat();
            }
        }
        this.minHeat = min == Float.MAX_VALUE ? 0 : min;
        this.heat = minHeat;
    }

    public void run() {
        //TODO simulation for player display
        // set some target heat value
        // compute the participating neutrons, react them with a fake reactive component (or simulate bool)
        // multiply by heatPerFission for heat generated, then figure out how much cooling is needed to cool that much heat
        // using all of the coolers at the site
        // this gives the coolant rate per cycle required to maintain the heat target and also allows computing fuel lifespan
        int toReact = computeParticipatingNeutrons();
        if (toReact != 0) {
            int reacted = reactiveComponent.react(toReact);
            addHeat(reacted * reactiveComponent.heatPerFission());
            reactiveComponent.reduceDurability(reacted);
        }

        if (coolantChannels != null) {
            for (CoolantChannel channel : coolantChannels) {
                if (heat <= minHeat) {
                    return;
                }
                if (channel == null) {
                    continue;
                }

                channel.applyCooling(this);
            }
        }
    }

    private int computeParticipatingNeutrons() {
        return Math.max(0, (int) (-neutronsPerCycle * reactivity * (heat - minHeat) / 100 + neutronsPerCycle));
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

    public void addNeutronsPerCycle(int neutrons) {
        this.neutronsPerCycle += neutrons;
    }

    public void setReactivity(float reactivity) {
        this.reactivity = reactivity;
    }

    public boolean hasFuel() {
        return this.reactiveComponent.durability() > 0;
    }
}
