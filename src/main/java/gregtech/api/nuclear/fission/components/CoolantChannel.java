package gregtech.api.nuclear.fission.components;

import gregtech.api.capability.ICoolantHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;

import java.util.List;

public class CoolantChannel extends ReactorComponent {

    private final Material coolant;
    private double weight;
    private int relatedFuelRodPairs;

    private ICoolantHandler inputHandler;
    private ICoolantHandler outputHandler;

    // Allows fission reactors to heat up less than a full liter of coolant.
    public double partialCoolant;

    public CoolantChannel(double maxTemperature, double thermalConductivity, Material coolant, double mass,
                          ICoolantHandler inputHandler, ICoolantHandler outputHandler) {
        super(coolant.getProperty(PropertyKey.COOLANT).getModeratorFactor(), maxTemperature, thermalConductivity, mass,
                true);
        this.coolant = coolant;
        this.weight = 0;
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    public static void normalizeWeights(List<CoolantChannel> effectiveCoolantChannels) {
        double sum = 0;
        for (CoolantChannel channel : effectiveCoolantChannels) {
            sum += channel.weight;
        }
        for (CoolantChannel channel : effectiveCoolantChannels) {
            channel.weight /= sum;
        }
    }

    public void addFuelRodPair() {
        relatedFuelRodPairs++;
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
        this.weight = relatedFuelRodPairs * 2;
    }

    public ICoolantHandler getInputHandler() {
        return inputHandler;
    }

    public ICoolantHandler getOutputHandler() {
        return outputHandler;
    }
}
