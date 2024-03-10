package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class FissionReactor {

    /**
     * The gas constant in J * K^-1 * mol^-1 if you want to use a different
     * set of units prepare thy life to become the worst of nightmares
     */
    public static final double R = 8.31446261815324;
    /**
     * Standard pressure in Pascal, corresponds to one standard atmosphere
     */
    public static final double standardPressure = 101325;

    private ReactorComponent[][] reactorLayout;
    private ArrayList<FuelRod> fuelRods;
    private ArrayList<ControlRod> controlRods;
    private ArrayList<CoolantChannel> coolantChannels;
    private ArrayList<ControlRod> effectiveControlRods;
    private ArrayList<CoolantChannel> effectiveCoolantChannels;

    // TODO: Verify usefulness of the following things
    private int numberOfComponents;

    private double totNeutronSources;
    private double avgGeometricFactorSlowNeutrons;
    private double avgGeometricFactorFastNeutrons;

    // TODO: Make this configurable
    private int geometricIntegrationSteps = 5;

    // Wtf is lSlow
    private double lSlow;
    private double lFast;
    private double kSlow;
    private double kFast;
    private double k;

    private double avgCoolantTemperature;
    private double controlRodFactor;

    // TODO: Determine tolerance range from config
    private double kEff; // criticality value, based on k

    // Is this still needed?
    private double avgBoilingPoint;
    private double avgAbsorption;
    private double avgPressure;
    private double avgModeration;

    /**
     * Thresholds important for determining the evolution of the reactor
     * ^^^ This is a very epic comment
     */
    public int criticalRodInsertion = 15; // determined by k value

    /**
     * Integers used on variables with direct player control for easier adjustments (normalize this to 0,1)
     */
    public int controlRodInsertion = 1;
    public int reactorDepth;
    public int reactorRadius;

    public boolean moderatorTipped; // set by the type of control rod in the reactor(prepInitialConditions)

    /**
     * Megawatts
     */
    public double power;
    public double prevPower;
    /**
     * Temperature of the reactor
     */
    public double temperature;
    public double pressure = standardPressure;
    public double exteriorPressure = standardPressure;
    /**
     * Temperature of boiling point in kelvin at standard pressure
     * Determined by a weighted sum of the individual coolant boiling points in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantBoilingPointStandardPressure;
    /**
     * Latent heat of vaporization in J/mol
     * Determined by a weighted sum of the individual heats of vaporization in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantHeatOfVaporization;
    /**
     * Equilibrium temperature in kelvin
     * Determined by a weighted sum of the individual coolant temperatures in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantBaseTemperature;
    public double fuelDepletion = 1;
    public double prevFuelDepletion;
    public double heatRemoved;
    public double neutronPoisonAmount; // can kill reactor if power is lowered and this value is high
    public double decayProductsAmount;
    public double envTemperature = 293; // maybe gotten from config per dim
    public double accumulatedHydrogen;

    public double maxTemperature = Double.MAX_VALUE;
    // Pascals
    public double maxPressure = 2000000;
    // In MW apparently
    public double maxPower = 3; // determined by the amount of fuel in reactor and neutron matricies

    /**
     *
     */
    public double coolingFactor; // same as the other cooling stuff(weighted with the channels)

    public double coolantMass;
    public double fuelMass;
    public double structuralMass;

    protected static double responseFunction(double target, double value, double criticalRate, double rate) {
        if (value <= 0) {
            if (rate > criticalRate) {
                return 0;
            } else {
                value = 0.1;
            }
        }
        return value * criticalRate / rate * Math.sqrt(target / value);
    }

    protected double responseFunctionTemperature(double target, double value, double criticalRate, double rate,
                                                        double equilibrium) {
        value = Math.max(0.1, value);
        rate = Math.max(0.1, rate);
        //return Math.max(value * criticalRate / rate * Math.sqrt(target / value), equilibrium);
        // TODO: just use abs of target - value maybe
        if (target > value)
            return Math.max(value + (criticalRate - rate) / ((this.coolantMass + this.structuralMass + this.fuelMass) * this.coolingFactor) - this.coolingFactor * Math.sqrt((target - value)/target) , equilibrium);
        else
            return Math.max(value + (criticalRate - rate) / ((this.coolantMass + this.structuralMass + this.fuelMass) * this.coolingFactor) - this.coolingFactor * Math.sqrt((value - target)/value) , equilibrium);
    }

    public FissionReactor(int size, int depth, int controlRodInsertion) {
        reactorLayout = new ReactorComponent[size][size];
        reactorDepth = depth;
        this.controlRodInsertion = controlRodInsertion;
        fuelRods = new ArrayList<>();
        controlRods = new ArrayList<>();
        coolantChannels = new ArrayList<>();
        effectiveControlRods = new ArrayList<>();
        effectiveCoolantChannels = new ArrayList<>();
    }

    public boolean canCoolantBoil() {
        return false;
    }

    public boolean explosionPossible() {
        return false;
    }

    public double voidFactor() {
        return this.canCoolantBoil() ? (this.temperature - this.envTemperature) / (double) this.pressure : 0.D;
    }

    public double criticalCoolantFlow() {
        return this.power / this.coolingFactor;
    }

    public void prepareThermalProperties() {
        int idRod = 0, idControl = 0, idChannel = 0;

        for (int i = 0; i < reactorLayout.length; i++) {
            for (int j = 0; j < reactorLayout[i].length; j++) {
                /*
                 * Check for null because the layout
                 * is in general not a square
                 */
                if (reactorLayout[i][j] != null && reactorLayout[i][j].isValid()) {
                    reactorLayout[i][j].setPos(i, j);
                    numberOfComponents++;
                    maxTemperature = Double.min(maxTemperature, reactorLayout[i][j].getMaxTemperature());
                    structuralMass += reactorLayout[i][j].getMass();
                    if (reactorLayout[i][j] instanceof FuelRod) {
                        reactorLayout[i][j].setID(idRod);
                        fuelRods.add((FuelRod) reactorLayout[i][j]);
                        idRod++;
                    }

                    if (reactorLayout[i][j] instanceof ControlRod) {
                        reactorLayout[i][j].setID(idControl);
                        controlRods.add((ControlRod) reactorLayout[i][j]);
                        idControl++;
                    }

                    if (reactorLayout[i][j] instanceof CoolantChannel) {
                        reactorLayout[i][j].setID(idChannel);
                        coolantChannels.add((CoolantChannel) reactorLayout[i][j]);
                        idChannel++;
                    }
                }
            }
        }
    }

    public void computeGeometry() {
        double[][] geometricMatrixSlowNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixFastNeutrons = new double[fuelRods.size()][fuelRods.size()];

        /*
         * We calculate geometric factor matrices to determine how many neutrons go from the i-th to the j-th fuel rod
         * This factor is different for slow and fast neutrons because they interact differently with the materials and
         * fuel
         */
        for (int i = 0; i < fuelRods.size(); i++) {
            for (int j = 0; j < i; j++) {
                double mij = 0;
                boolean pathIsClear = true;
                ArrayList<ControlRod> controlRodsHit = new ArrayList<ControlRod>();
                ArrayList<CoolantChannel> coolantChannelsHit = new ArrayList<CoolantChannel>();

                /*
                 * Geometric factor calculation is done by (rough) numerical integration along a straight path between
                 * the two cells
                 */
                for (int t = 0; t < geometricIntegrationSteps; t++) {
                    double[] pos = { .5, .5 };
                    pos[0] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[0]) *
                            ((float) t / geometricIntegrationSteps) + fuelRods.get(i).getPos()[0];
                    pos[1] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[1]) *
                            ((float) t / geometricIntegrationSteps) + fuelRods.get(i).getPos()[1];
                    ReactorComponent component = reactorLayout[(int) Math.floor(pos[0])][(int) Math.floor(pos[1])];

                    if (component == null) {
                        continue;
                    }
                    mij += component.getModerationFactor();

                    /*
                     * For simplicity, we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel
                     * rods are ignored as obstructed
                     */
                    if (component instanceof FuelRod && component.samePositionAs(fuelRods.get(i)) &&
                            component.samePositionAs(fuelRods.get(j))) {
                        pathIsClear = false;
                        break;
                    }

                    /*
                     * We keep track of which active elements we hit, so we can determine how important they are
                     * relative to the others later
                     */
                    if (component instanceof ControlRod) {
                        if (!controlRodsHit.contains(component)) {
                            controlRodsHit.add((ControlRod) component);
                        }
                    } else if (component instanceof CoolantChannel) {
                        if (!coolantChannelsHit.contains(component)) {
                            coolantChannelsHit.add((CoolantChannel) component);
                        }
                    }
                }

                /*
                 * The actual calculation of the geometric factors, fast neutrons are randomly converted into slow
                 * neutrons along the path, we pretend that fuel rods are infinitely tall and thin for simplicity
                 * This means the fraction of slow neutrons will go as (1-exp(-m * x))/x where x is the distance between
                 * the cells
                 * The fraction of fast neutrons is simply one minus the fraction of slow neutrons
                 */
                if (pathIsClear) {
                    mij /= geometricIntegrationSteps;
                    geometricMatrixSlowNeutrons[i][j] = geometricMatrixSlowNeutrons[j][i] = (1. -
                            Math.exp(-mij * fuelRods.get(i).getDistance(fuelRods.get(j)))) /
                            fuelRods.get(i).getDistance(fuelRods.get(j));
                    geometricMatrixFastNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = 1. /
                            fuelRods.get(i).getDistance(fuelRods.get(j)) - geometricMatrixSlowNeutrons[i][j];

                    for (ControlRod rod : controlRodsHit) {
                        rod.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }

                    for (CoolantChannel channel : coolantChannelsHit) {
                        channel.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }
                }
            }
        }

        /*
         * We now use the data we have on the geometry to calculate the reactor's stats
         */
        double avgGeometricFactorSlowNeutrons = 0.;
        double avgGeometricFactorFastNeutrons = 0.;

        double avgHighEnergyFissionFactor = 0.;
        double avgLowEnergyFissionFactor = 0.;
        double avgHighEnergyCaptureFactor = 0.;
        double avgLowEnergyCaptureFactor = 0.;

        double avgFuelRodDistance = 0.;

        for (FuelRod i : fuelRods) {
            for (FuelRod j : fuelRods) {
                avgGeometricFactorSlowNeutrons += geometricMatrixSlowNeutrons[i.getID()][j.getID()];
                avgGeometricFactorFastNeutrons += geometricMatrixFastNeutrons[i.getID()][j.getID()];

                avgFuelRodDistance += i.getDistance(j);
            }
            totNeutronSources += i.getNeutronSourceIntensity();

            avgHighEnergyFissionFactor += i.getHEFissionFactor();
            avgLowEnergyFissionFactor += i.getLEFissionFactor();
            avgHighEnergyCaptureFactor += i.getHECaptureFactor();
            avgLowEnergyCaptureFactor += i.getLECaptureFactor();

        }

        avgGeometricFactorSlowNeutrons *= 0.125 / fuelRods.size();
        avgGeometricFactorFastNeutrons *= 0.125 / fuelRods.size();

        avgHighEnergyFissionFactor /= fuelRods.size();
        avgLowEnergyFissionFactor /= fuelRods.size();
        avgHighEnergyCaptureFactor /= fuelRods.size();
        avgLowEnergyCaptureFactor /= fuelRods.size();

        avgFuelRodDistance /= 2. * fuelRods.size();

        lSlow = avgFuelRodDistance / (2200. * avgLowEnergyCaptureFactor);
        lFast = avgFuelRodDistance / (15000000. * avgHighEnergyCaptureFactor);

        kSlow = avgLowEnergyFissionFactor / avgLowEnergyCaptureFactor * avgGeometricFactorSlowNeutrons;
        kFast = avgHighEnergyFissionFactor / avgHighEnergyCaptureFactor * avgGeometricFactorFastNeutrons;

        k = (kSlow + kFast) * reactorDepth / (1. + reactorDepth);

        /*
         * We give each control rod and coolant channel a weight depending on how many fuel rods they affect
         */
        this.computeControlRodWeights();
        this.computeCoolantChannelWeights();

        controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods);
        avgCoolantTemperature /= coolantChannels.size();

        this.prepareInitialConditions();

        kEff = 1. * k;
    }

    /**
     * Loops over all the control rods, determines which ones actually affect reactivity, and gives them a weight
     * depending on how many fuel rods they affect
     */
    protected void computeControlRodWeights() {
        for (ControlRod rod : controlRods) {
            rod.computeWeightFromFuelRodMap();
            if (rod.getWeight() > 0) {
                effectiveControlRods.add(rod);
            }
        }
        ControlRod.normalizeWeights(effectiveControlRods);
    }

    /**
     * Loops over all the coolant channels, determines which ones actually affect reactivity, and gives them a weight
     * depending on how many fuel rods they affect
     */
    protected void computeCoolantChannelWeights() {
        for (CoolantChannel channel : coolantChannels) {
            channel.computeWeightFromFuelRodMap();
            if (channel.getWeight() > 0) {
                effectiveCoolantChannels.add(channel);
            }
        }
        CoolantChannel.normalizeWeights(effectiveCoolantChannels);
    }

    public void prepareInitialConditions() {
        for (CoolantChannel channel : effectiveCoolantChannels) {

            CoolantProperty prop = channel.getCoolant().getProperty(PropertyKey.COOLANT);

            coolantBaseTemperature += channel.getCoolant().getFluid().getTemperature() *
                    channel.getWeight();
            coolantBoilingPointStandardPressure += prop.getBoilingPoint() *
                    channel.getWeight();
            avgAbsorption += prop.getAbsorption() *
                    channel.getWeight();
            avgModeration += prop.getModerationFactor() *
                    channel.getWeight();
            avgPressure += prop.getPressure() *
                    channel.getWeight();
            coolantHeatOfVaporization += prop.getHeatOfVaporization() *
                    channel.getWeight();
            coolingFactor += prop.getCoolingFactor() *
                    channel.getWeight();
        }

        temperature = coolantBaseTemperature;
    }

    /**
     * Consumes the coolant.
     * Calculates the heat removed by the coolant based on an amalgamation of different equations.
     * It is not particularly realistic, but allows for some fine-tuning to happen.
     * Heat removed is proportional to the surface area of the coolant channel (which is equivalent to the reactor's depth),
     * as well as the flow rate of coolant and the difference in temperature between the reactor and the coolant
     */
    public void makeCoolantFlow(int flowRate) {
        for (CoolantChannel channel : coolantChannels) {
            FluidStack tryFluidDrain = channel.getInputHandler().getFluidTank().drain(flowRate, false);
            if(tryFluidDrain != null) {
                int drained = tryFluidDrain.amount;

                Material coolant = channel.getCoolant();

                int remainingSpace = channel.getOutputHandler().getFluidTank().getCapacity() -
                        channel.getOutputHandler().getFluidTank().getFluidAmount();
                int actualFlowRate = Math.min(remainingSpace, drained);
                FluidStack HPCoolant = new FluidStack(
                        coolant.getProperty(PropertyKey.COOLANT).getHotHPCoolant().getFluid(), actualFlowRate);

                channel.getInputHandler().getFluidTank().drain(actualFlowRate, true);

                if(this.temperature > this.coolantBoilingPoint()) {
                    channel.getOutputHandler().getFluidTank().fill(HPCoolant, true);
                }

                this.coolantMass += actualFlowRate * coolant.getMass();

                this.heatRemoved += coolant.getProperty(PropertyKey.COOLANT).getCoolingFactor()
                        * this.reactorDepth * actualFlowRate * (this.coolantBoilingPoint(coolant) - coolant.getFluid().getTemperature());
            }
        }
        this.coolantMass /= 1000;
    }

    /**
     * The thermodynamics is not completely realistic, but it's close enough for simple things like this, the boiling
     * point depends on pressure
     */
    protected double coolantBoilingPoint() {
        return 1. / (1. / this.coolantBoilingPointStandardPressure -
                R * Math.log(this.pressure / standardPressure) / this.coolantHeatOfVaporization);
    }

    protected double coolantBoilingPoint(Material coolant) {
        if (coolant.getProperty(PropertyKey.COOLANT).getBoilingPoint() == 0) {
            return coolantBoilingPoint();
        }
        return 1. / (1. / coolant.getProperty(PropertyKey.COOLANT).getBoilingPoint() -
                R * Math.log(this.pressure / standardPressure) / coolant.getProperty(PropertyKey.COOLANT).getHeatOfVaporization());
    }

    public void updateTemperature() {
        this.temperature = responseFunctionTemperature(this.maxTemperature, this.temperature, this.power * 1000000,
                this.heatRemoved, this.coolantBaseTemperature);
        this.heatRemoved = 0;
    }

    public void updatePressure() {
        this.pressure = responseFunction(
                this.temperature <= this.coolantBoilingPoint() ? this.exteriorPressure : 1000. * R * this.temperature,
                this.pressure, 1., 1.);
    }

    public void updateNeutronPoisoning() {
        this.neutronPoisonAmount += Math.max(0., this.prevPower - this.power);
        this.neutronPoisonAmount *= 0.99;
    }

    public double getDecayHeat() {
        return this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1;
    }

    public double voidContribution() {
        return this.temperature > this.coolantBoilingPoint() ? this.voidFactor() * this.maxPressure : 0.;
    }

    public void updatePower() {
        this.prevPower = this.power;
        this.prevFuelDepletion = this.fuelDepletion;
        this.power = responseFunction(this.realMaxPower(), this.power,
                this.criticalRodInsertion + this.voidContribution(), this.controlRodInsertion);
        this.fuelDepletion = Math.min(this.fuelDepletion + 0.001 * this.power / this.maxPower, 1.);
        this.decayProductsAmount += Math.max(this.fuelDepletion - this.prevFuelDepletion, 0.);
        this.decayProductsAmount *= 0.99;
    }

    public double realMaxPower() {
        if (this.moderatorTipped && (this.controlRodInsertion <= 9 && this.controlRodInsertion >= 7)) {
            return this.maxPower * 1.1;
        } else if (this.controlRodInsertion > this.criticalRodInsertion || this.fuelDepletion >= 1.) {
            return this.getDecayHeat();
        } else {
            return this.maxPower;
        }
    }

    public boolean checkForMeltdown() {
        return this.temperature > this.maxTemperature;
    }

    public boolean checkForExplosion() {
        return this.pressure > this.maxPressure;
    }

    public boolean checkForSecondaryExplosion() {
        return this.accumulatedHydrogen > 0.;
    }

    public void addComponent(ReactorComponent component, int x, int y) {
        reactorLayout[x][y] = component;
    }
}
