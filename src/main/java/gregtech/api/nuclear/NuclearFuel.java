package gregtech.api.nuclear;

import gregtech.api.nuclear.fuels.NuclearFuels;
import gregtech.api.unification.material.Material;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class NuclearFuel {
    private List<Pair<Material, Double>> nuclearMaterials;
    private final double duration;
    private final double temperatureCoefficient;
    private List<Pair<Double, Double>> delayedNeutronsGroups = new ArrayList<>();
    private final double[] cs_vector;

    public NuclearFuel(List<Pair<Material, Double>> nuclearMaterials, double duration, double temperatureCoefficient) {
        this.nuclearMaterials = nuclearMaterials;
        this.duration = duration;
        this.temperatureCoefficient = temperatureCoefficient;
        this.cs_vector = getMacroCrossSections();
    }

    double[] getMacroCrossSections() {
        double[] mcs = new double[]{0, 0, 0, 0};
        for (Pair<Material, Double> pair : nuclearMaterials) {
            double[] pairCs = pair.getLeft().getNuclearCrossSections();
            mcs[0] += pairCs[0] * pair.getRight();
            mcs[1] += pairCs[1] * pair.getRight();
            mcs[2] += pairCs[2] * pair.getRight();
            mcs[3] += pairCs[3] * pair.getRight();
        }
        for (int i = 0, mcsLength = mcs.length; i < mcsLength; i++) {
            mcs[i] = mcs[i] / nuclearMaterials.size();
        }
        return mcs;
    }

    public NuclearFuel setDelayedNeutronsGroups(List<Pair<Double, Double>> groups) {
        delayedNeutronsGroups = groups;
        return this;
    }

    List<Pair<Double, Double>> getDelayedNeutronsGroups() {
        return delayedNeutronsGroups;
    }

    double getTemperatureCoefficient() {
        return temperatureCoefficient;
    }

    public double getDuration() {
        return duration;
    }

    public double[] getCs_vector() {
        return cs_vector;
    }
}
