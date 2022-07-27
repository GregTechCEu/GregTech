package gregtech.api.nuclear.components;

import gregtech.api.nuclear.ReactorComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ControlRod extends ReactorComponent {
    private int weight;
    private final boolean tipModeration;
    private float Insertion;
    private final List<Pair<Integer, Integer>> fuelRodPairs = new ObjectArrayList<>();

    public ControlRod(int maxTemperature, boolean tipModeration, double thermalConductivity, float insertion) {
        super(true, maxTemperature, 0, thermalConductivity);
        this.tipModeration = tipModeration;
        this.Insertion = insertion;
        this.weight = 0;
    }

    public static void NormalizeWeights() {
        float s = 0;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void addFuelRodPairToMap(int idA, int idB) {
        fuelRodPairs.add(Pair.of(idA, idB));
    }

    List<Pair<Integer, Integer>> getFuelRodPairMap() {
        return fuelRodPairs;
    }

    public float getInsertion() {
        return Insertion;
    }

    public void setInsertion(float insertion) {
        Insertion = insertion;
    }

    public void increaseWeight() {
        weight++;
    }

    public boolean hasModeratorTip() {
        return tipModeration;
    }

    public void computeWeightFromFuelRodMap() {
        this.weight = fuelRodPairs.size() - 1;
    }
}
