package gregtech.api.nuclear.fission.components;

import gregtech.api.capability.ICoolantHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class CoolantChannel extends ReactorComponent {

    private final Material coolant;
    private double weight;
    private final List<Pair<FuelRod, FuelRod>> fuelRodPairs = new ObjectArrayList<>();

    private ICoolantHandler inputHandler;
    private ICoolantHandler outputHandler;

    // Allows fission reactors to heat up less than a full liter of coolant.
    public double partialCoolant;

    public CoolantChannel(double maxTemperature, double thermalConductivity, Material coolant, double mass,
                          ICoolantHandler inputHandler, ICoolantHandler outputHandler) {
        super(coolant.getProperty(PropertyKey.COOLANT).getModerationFactor(), maxTemperature, thermalConductivity, mass,
                true);
        this.coolant = coolant;
        this.weight = 0;
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    public static void normalizeWeights(ArrayList<CoolantChannel> effectiveCoolantChannels) {
        double sum = 0;
        for (CoolantChannel channel : effectiveCoolantChannels) {
            sum += channel.weight;
        }
        for (CoolantChannel channel : effectiveCoolantChannels) {
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

    public ICoolantHandler getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(ICoolantHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public ICoolantHandler getOutputHandler() {
        return outputHandler;
    }

    public void setOutputHandler(ICoolantHandler outputHandler) {
        this.outputHandler = outputHandler;
    }
}
