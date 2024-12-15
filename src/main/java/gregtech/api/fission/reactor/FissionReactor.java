package gregtech.api.fission.reactor;

import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.FissionComponent;

import gregtech.api.fission.component.NeutronEmitter;
import gregtech.api.fission.component.ReactiveComponent;

import gregtech.api.fission.component.ControlRod;
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

public class FissionReactor implements ReactorPathWalker {

    private final Map<Vec2i, ReactionSite> sites = new Object2ReferenceOpenHashMap<>();
    private final float[] radiatedNeutronsCached = new float[ComponentDirection.VALUES.length];
    private final List<NeutronPathData> radiatedNeutrons = new ArrayList<>();

    private final FissionComponent[][] matrix;
    private final int[][] durabilities;
    private final int size;
    private final int maxHeat;

    private boolean needsGeometryCompute = true;
    private boolean needsComponentRecompute = true;
    private boolean needsRadiationRecompute = true;

    private boolean meltdown;

    public FissionReactor(int size, int maxHeat) {
        this.matrix = new FissionComponent[size][size];
        this.durabilities = new int[size][size];
        this.size = size;
        this.maxHeat = maxHeat;
    }

    public FissionComponent @NotNull [] @NotNull [] matrix() {
        return matrix;
    }

    /**
     * Compute and set up the reactor geometry
     */
    public void computeGeometry() {
        if (!needsGeometryCompute) {
            return;
        }

        // track positions of important components
        Map<Vec2i, NeutronEmitter> emitters = new Object2ReferenceOpenHashMap<>();
        Map<ReactiveComponent, ReactionSite> componentSites = new Reference2ReferenceOpenHashMap<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                FissionComponent component = matrix[r][c];
                if (component instanceof ReactiveComponent reactiveComponent) {
                    // gather adjacent components
                    List<CoolantChannel> coolantChannels = new ArrayList<>();
                    List<ControlRod> controlRods = new ArrayList<>();
                    for (int dR = -1; dR <= 1; dR++) {
                        int offsetR = r + dR;
                        if (offsetR < 0 || offsetR >= size) {
                            continue;
                        }
                        for (int dC = -1; dC <= 1; dC++) {
                            int offsetC = c + dC;
                            if (offsetC < 0 || offsetC >= size) {
                                continue;
                            }

                            FissionComponent comp = matrix[offsetR][offsetC];
                            if (comp instanceof CoolantChannel channel) {
                                coolantChannels.add(channel);
                            }
                            if (comp instanceof ControlRod controlRod) {
                                controlRods.add(controlRod);
                            }
                        }
                    }

                    if (coolantChannels.isEmpty() || controlRods.isEmpty()) {
                        // TODO tell player a fuel rod is missing things
                        continue;
                    }

                    ReactionSite site = new ReactionSite(reactiveComponent, coolantChannels, controlRods);
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
            float generated = emitter.generateNeutrons();

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
                    radiatedNeutrons.add(data);
                } else if (component instanceof ReactiveComponent reactiveComponent) {
                    ReactionSite site = componentSites.get(reactiveComponent);
                    assert site != null;
                    site.addNeutronsPerCycle(data);
                } else {
                    durabilityPerComponent.put(component, durabilityPerComponent.getInt(component) + 1);
                }
            }

            if (emitter instanceof ReactiveComponent component) {
                ReactionSite site = componentSites.get(component);
                assert site != null;

                // iterate the data for the current reactive component to find the reactivity for the site
                for (ReactivityPathData data : reactivityData) {
                    site.addReactivity(data);
                }
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

        needsGeometryCompute = false;
    }

    @Override
    public void walkPath(@NotNull List<NeutronPathData> neutronData, @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                         @NotNull ComponentDirection direction, int startR, int startC, float neutrons) {
        int r = startR + direction.offsetY();
        if (r < 0 || r >= size) {
            neutronData.add(NeutronPathData.of(null, direction, neutrons));
            return;
        }

        int c = startC + direction.offsetX();
        if (c < 0 || c >= size) {
            neutronData.add(NeutronPathData.of(null, direction, neutrons));
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
        if (needsGeometryCompute) {
            return false;
        }

        if (meltdown) {
            return false;
        }

        int empty = 0;
        for (ReactionSite site : sites.values()) {
            if (needsComponentRecompute) {
                site.recompute();
            }

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

        if (needsRadiationRecompute) {
            computeRadiation();
        }

        for (int i = 0; i < radiatedNeutronsCached.length; i++) {
            float radiation = radiatedNeutronsCached[i];
            if (radiation > 0) {
                emitRadiation(ComponentDirection.VALUES[i], radiation);
            }
        }

        return true;
    }

    private void computeRadiation() {
        Arrays.fill(radiatedNeutronsCached, 0);
        for (NeutronPathData data : radiatedNeutrons) {
            radiatedNeutronsCached[data.direction().ordinal()] += data.getNeutrons();
        }
    }

    private void emitRadiation(@NotNull ComponentDirection direction, float amount) {
        // TODO radiation implementation
        System.out.println(amount + " radiation " + direction);
    }

    private void meltdown(float heat) {
        this.meltdown = true;
        // TODO meltdown implementation
        System.err.println("MELTDOWN with " + heat);
    }

    /**
     * Trigger recomputes which affect the reactor while it is running
     */
    public void triggerRuntimeRecompute() {
        this.needsComponentRecompute = true;
        this.needsRadiationRecompute = true;
    }

    public int size() {
        return this.size;
    }
}
