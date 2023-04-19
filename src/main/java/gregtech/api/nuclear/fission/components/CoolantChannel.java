package gregtech.api.nuclear.fission.components;

import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class CoolantChannel extends ReactorComponent {
    private final Material coolant;
    private double weight;
    private final List<Pair<FuelRod, FuelRod>> fuelRodPairs = new ObjectArrayList<>();


    public CoolantChannel(double maxTemperature, double thermalConductivity, Material coolant) {
        super(0/*coolant.getCoolantProperties().getModeratorFactor()*/, maxTemperature, thermalConductivity, true);
        this.coolant = coolant;
        this.weight = 0;
    }

    public static void NormalizeWeights(ArrayList<CoolantChannel> effective_coolant_channels) {
        double sum = 0;
        for (CoolantChannel channel : effective_coolant_channels) {
            sum += channel.weight;
        }
        for (CoolantChannel channel : effective_coolant_channels) {
            channel.weight /= sum;
        }
    }

    public void addFuelRodPairToMap(FuelRod fuelRodA, FuelRod fuelRodB) {
        fuelRodPairs.add(Pair.of(fuelRodA, fuelRodB));
    }

    List<Pair<FuelRod, FuelRod>> getFuelRodPairMap() {
        return fuelRodPairs;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Material getCoolant() {
        return coolant;
    }

    public void computeWeightFromFuelRodMap() {
        this.weight = fuelRodPairs.size() * 2;
    }

}
