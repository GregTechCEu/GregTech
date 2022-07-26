package gregtech.api.nuclear.components;

import gregtech.api.nuclear.ReactorComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CoolantChannel extends ReactorComponent {
    private final Coolant coolant;
    private int weight;
    private final List<Pair<Integer, Integer>> fuelRodPairs = new ObjectArrayList<>();


    CoolantChannel(int maxTemperature, float thermalConductivity, Coolant coolant) {
        super(true, maxTemperature, coolant.getModeratorFactor(), thermalConductivity);
        this.coolant = coolant;
        this.weight = 0;
    }

    public void addFuelRodPairToMap(int idA, int idB) {
        fuelRodPairs.add(Pair.of(idA, idB));
    }

    List<Pair<Integer, Integer>> getFuelRodPairMap() {
        return fuelRodPairs;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Coolant getCoolant() {
        return coolant;
    }

    public void computeWeightFromFuelRodMap() {
        this.weight = fuelRodPairs.size() - 1;
    }
}
