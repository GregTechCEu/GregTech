package gregtech.api.nuclear.fission.fuels;

import gregtech.api.unification.material.Material;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class NuclearFuel {
    private List<Pair<Material, Double>> nuclearMaterials;
    private final double duration;
    private final double temperatureCoefficient;
    private Double[][] delayedNeutronsGroups = new Double[6][];
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
            double[] pairCs = new double[]{0, 0}/*pair.getLeft().getNuclearCrossSections()*/;
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

    public NuclearFuel setDelayedNeutronsGroups(Double[][] groups) {
        delayedNeutronsGroups = groups;
        return this;
    }

    public Double[][] getDelayedNeutronsGroups() {
        return delayedNeutronsGroups;
    }

    public double getTemperatureCoefficient() {
        return temperatureCoefficient;
    }

    public double getDuration() {
        return duration;
    }

    public double[] getCrossSectionVector() {
        return cs_vector;
    }
}
