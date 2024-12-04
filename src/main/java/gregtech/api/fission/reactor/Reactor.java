package gregtech.api.fission.reactor;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.FissionComponent;

import gregtech.api.fission.component.NeutronEmitter;
import gregtech.api.fission.component.ReactiveComponent;

import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.util.math.Vec2i;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Reactor implements ReactorPathWalker {

    private final Map<Vec2i, ReactionSite> sites = new Object2ReferenceOpenHashMap<>();
    private final int[] radiatedNeutrons = new int[ComponentDirection.VALUES.length];

    private final FissionComponent[][] matrix;
    private final int[][] durabilities;
    private final int size;
    private final int maxHeat;

    private boolean meltdown;

    public Reactor(int size, int maxHeat) {
        this.matrix = new FissionComponent[size][size];
        this.durabilities = new int[size][size];
        this.size = size;
        this.maxHeat = maxHeat;
    }

    //TODO remove this and do it properly
    public void populate(@NotNull Consumer<FissionComponent[][]> consumer) {
        consumer.accept(matrix);
    }

    /**
     * Reset the reactor for layout changes
     * <p>
     * A new reactor must be created for size changes
     */
    public void reset() {
        for (int i = 0; i < size; i++) {
            Arrays.fill(matrix[i], null);
            Arrays.fill(durabilities[i], 0);
        }
        sites.clear();
        Arrays.fill(radiatedNeutrons, 0);
    }

    /**
     * Compute and set up the reactor geometry
     */
    public void computeGeometry() {
        // track positions of important components
        Map<Vec2i, NeutronEmitter> emitters = new Object2ReferenceOpenHashMap<>();
        Map<ReactiveComponent, ReactionSite> componentSites = new Reference2ReferenceOpenHashMap<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                FissionComponent component = matrix[r][c];
                if (component instanceof ReactiveComponent reactiveComponent) {
                    // gather adjacent cooling channels
                    CoolantChannel[] coolantChannels = new CoolantChannel[ComponentDirection.VALUES.length + 1];
                    if (c > 0 && matrix[r][c - 1] instanceof CoolantChannel channel) {
                        coolantChannels[ComponentDirection.LEFT.ordinal()] = channel;
                    }
                    if (c < size - 1 && matrix[r][c + 1] instanceof CoolantChannel channel) {
                        coolantChannels[ComponentDirection.RIGHT.ordinal()] = channel;
                    }
                    if (r > 0 && matrix[r - 1][c] instanceof CoolantChannel channel) {
                        coolantChannels[ComponentDirection.UP.ordinal()] = channel;
                    }
                    if (r < size - 1 && matrix[r + 1][c] instanceof CoolantChannel channel) {
                        coolantChannels[ComponentDirection.DOWN.ordinal()] = channel;
                    }
                    if (component instanceof CoolantChannel channel) {
                        coolantChannels[ComponentDirection.VALUES.length + 1] = channel;
                    }

                    ReactionSite site = new ReactionSite(reactiveComponent, coolantChannels);
                    sites.put(new Vec2i(r, c), site);
                    componentSites.put(reactiveComponent, site);
                }
                if (component instanceof NeutronEmitter emitter) {
                    emitters.put(new Vec2i(r, c), emitter);
                }
            }
        }

        Reference2IntMap<FissionComponent> durabilityPerComponent = new Reference2IntOpenHashMap<>();
        for (var entry : emitters.entrySet()) {
            Vec2i pos = entry.getKey();
            NeutronEmitter emitter = entry.getValue();
            int emitterR = pos.x();
            int emitterC = pos.y();
            int generated = emitter.generateNeutrons();

            // trace paths neutrons will take when emitted
            List<NeutronPathData> neutronData = new ArrayList<>();
            List<ReactivityPathData> reactivityData = new ArrayList<>();
            for (ComponentDirection direction : ComponentDirection.VALUES) {
                walkPath(neutronData, reactivityData, emitter, direction, emitterR, emitterC, generated);
            }

            // compute the change per emission cycle
            for (NeutronPathData data : neutronData) {
                FissionComponent component = data.component();
                if (component == null) {
                    // radiation exits out the walls
                    ComponentDirection direction = data.direction();
                    radiatedNeutrons[direction.ordinal()] += data.neutrons();
                    continue;
                }

                if (component instanceof ReactiveComponent reactiveComponent) {
                    ReactionSite site = componentSites.get(reactiveComponent);
                    assert site != null;
                    site.addNeutronsPerCycle(data.neutrons());
                } else {
                    durabilityPerComponent.put(component, durabilityPerComponent.getInt(component) + 1);
                }
            }

            if (emitter instanceof ReactiveComponent component) {
                ReactionSite site = componentSites.get(component);
                assert site != null;

                // iterate the data for the current reactive component to find the reactivity for the site
                float reactivity = 0;
                for (ReactivityPathData data : reactivityData) {
                    reactivity += data.reactivity();
                }
                site.setReactivity(reactivity);
            }
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                for (var entry : durabilityPerComponent.reference2IntEntrySet()) {
                    if (matrix[r][c] == entry.getKey()) {
                        durabilities[r][c] = entry.getIntValue();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void walkPath(@NotNull List<NeutronPathData> neutronData, @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                         @NotNull ComponentDirection direction, int startR, int startC, int neutrons) {
        int r = startR + direction.offsetY();
        if (r < 0 || r >= size) {
            neutronData.add(new NeutronPathData(null, direction, neutrons));
            return;
        }

        int c = startC + direction.offsetX();
        if (c < 0 || c >= size) {
            neutronData.add(new NeutronPathData(null, direction, neutrons));
            return;
        }

        // recursive for some components
        FissionComponent component = matrix[r][c];
        component.processNeutronPath(this, neutronData, reactivityData, source, direction, r, c, neutrons);
    }

    /**
     * @return if running was successful
     */
    public boolean run() {
        if (meltdown) {
            return false;
        }

        int empty = 0;
        for (ReactionSite site : sites.values()) {
            site.run();
            if (site.heat() > maxHeat) {
                meltdown(site.heat());
                return false;
            }
            if (!site.hasFuel()) {
                empty++;
            }
        }
        if (empty == sites.size()) {
            System.out.println("Out of fuel");
            return false;
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                matrix[r][c].reduceDurability(durabilities[r][c]);
            }
        }
        for (int i = 0; i < radiatedNeutrons.length; i++) {
            int radiation = radiatedNeutrons[i];
            if (radiation != 0) {
                emitRadiation(ComponentDirection.VALUES[i], radiation);
            }
        }

        return true;
    }

    private void emitRadiation(@NotNull ComponentDirection direction, int amount) {
        // TODO radiation implementation
        System.out.println(amount + " radiation " + direction);
    }

    private void meltdown(float heat) {
        this.meltdown = true;
        // TODO meltdown implementation
        System.err.println("MELTDOWN with " + heat);
    }

    public int size() {
        return this.size;
    }
}
