package gregtech.api.nuclear;

import gregtech.api.unification.material.Material;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class NuclearFuel {
    private List<Pair<Material, Float>> nuclearMaterials;
    private final int duration;
    private final float temperatureCoefficient;
    private List<Pair<Float, Float>> delayedNeutronsGroups = new ArrayList<>();
    private final double[] cs_vector;

    NuclearFuel(List<Pair<Material, Float>> nuclearMaterials, int duration, float temperatureCoefficient) {
        this.nuclearMaterials = nuclearMaterials;
        this.duration = duration;
        this.temperatureCoefficient = temperatureCoefficient;
        this.cs_vector = getMacroCrossSections();
    }

    double[] getMacroCrossSections() {
        double[] mcs = new double[]{0, 0, 0, 0};
        for (Pair<Material, Float> pair : nuclearMaterials) {
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

    void setDelayedNeutronsGroups(List<Pair<Float, Float>> groups) {
        delayedNeutronsGroups = groups;
    }

    List<Pair<Float, Float>> getDelayedNeutronsGroups() {
        return delayedNeutronsGroups;
    }

    float getTemperatureCoefficient() {
        return temperatureCoefficient;
    }

    public int getDuration() {
        return duration;
    }

    public double[] getCs_vector() {
        return cs_vector;
    }
}
