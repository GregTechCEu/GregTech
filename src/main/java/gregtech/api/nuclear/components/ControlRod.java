package gregtech.api.nuclear.components;

import gregtech.api.nuclear.ReactorComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ControlRod extends ReactorComponent {
    private int weight;
    private final boolean tipModeration;
    private float insertion;
    private final List<Pair<FuelRod, FuelRod>> fuelRodPairs = new ObjectArrayList<>();

    public ControlRod(int maxTemperature, boolean tipModeration, double thermalConductivity, float insertion) {
        super(true, maxTemperature, 0, thermalConductivity);
        this.tipModeration = tipModeration;
        this.insertion = insertion;
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

    public void addFuelRodPairToMap(FuelRod fuelRodA, FuelRod fuelRodB) {
        fuelRodPairs.add(Pair.of(fuelRodA, fuelRodB));
    }

    List<Pair<FuelRod, FuelRod>> getFuelRodPairMap() {
        return fuelRodPairs;
    }

    public float getInsertion() {
        return insertion;
    }

    public void setInsertion(float insertion) {
        this.insertion = insertion;
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

    @Override
    public ControlRod copy() {
        return new ControlRod(getMaxTemperature(), tipModeration, getThermalConductivity(), insertion);
    }
}
