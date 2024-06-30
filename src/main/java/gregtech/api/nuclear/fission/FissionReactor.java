package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.CoolantProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.ConfigHolder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class FissionReactor {

    /**
     * The gas constant in J * K^-1 * mol^-1 if you want to use a different set of units prepare thy life to become the
     * worst of nightmares
     */
    public static final double R = 8.31446261815324;
    /**
     * Standard pressure in Pascal, corresponds to one standard atmosphere
     */
    public static final double standardPressure = 101325;

    /**
     * The starting temperature of the reactor in Kelvin
     */
    public static final double roomTemperature = 273;

    /**
     * Boiling point of air at standard pressure in Kelvin
     */
    public static final double airBoilingPoint = 78.8;

    private ReactorComponent[][] reactorLayout;
    private final ArrayList<FuelRod> fuelRods;
    private final ArrayList<ControlRod> controlRods;
    private final ArrayList<CoolantChannel> coolantChannels;
    private final ArrayList<ControlRod> effectiveControlRods;
    private final ArrayList<CoolantChannel> effectiveCoolantChannels;

    private double k;

    private double controlRodFactor;

    public double kEff; // criticality value, based on k

    private double avgPressure;

    /**
     * Thresholds important for determining the evolution of the reactor ^^^ This is a very epic comment
     */
    public int criticalRodInsertion = 15; // determined by k value

    /**
     * Integers used on variables with direct player control for easier adjustments (normalize this to 0,1)
     */
    public double controlRodInsertion = 1;
    public int reactorDepth;
    public double reactorRadius;

    public boolean moderatorTipped; // set by the type of control rod in the reactor(prepInitialConditions)

    /**
     * Megawatts
     */
    public double power;
    /**
     * Temperature of the reactor
     */
    public double temperature = roomTemperature;
    public double pressure = standardPressure;
    public double exteriorPressure = standardPressure;
    /**
     * Temperature of boiling point in kelvin at standard pressure Determined by a weighted sum of the individual
     * coolant boiling points in {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantBoilingPointStandardPressure;
    /**
     * Latent heat of vaporization in J/mol Determined by a weighted sum of the individual heats of vaporization in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantHeatOfVaporization;
    /**
     * Equilibrium temperature in kelvin Determined by a weighted sum of the individual coolant temperatures in
     * {@link FissionReactor#prepareInitialConditions()}
     */
    public double coolantBaseTemperature;
    public double maxFuelDepletion = 1;
    public double fuelDepletion = -1;
    public double prevFuelDepletion;
    public double heatRemoved;
    public double neutronPoisonAmount; // can kill reactor if power is lowered and this value is high
    public double decayProductsAmount;
    public double envTemperature = roomTemperature; // maybe gotten from config per dim
    public double accumulatedHydrogen;

    public double maxTemperature = 2000;
    // Pascals
    public double maxPressure = 15000000;
    // In MW apparently
    public double maxPower = 3; // determined by the amount of fuel in reactor and neutron matricies
    public static double zircaloyHydrogenReactionTemperature = 1500;

    public double surfaceArea;
    public static double thermalConductivity = 45; // 45 W/(m K), for steel
    public static double wallThickness = 0.1;
    public static double coolantWallThickness = 0.06; // Ideal for a 1-m diameter steel pipe with the given maximum
    // pressure
    public static double specificHeatCapacity = 420; // 420 J/(kg K), for steel
    public static double convectiveHeatTransferCoefficient = 10; // 10 W/(m^2 K), for slow-moving air

    public static double powerDefectCoefficient = 0.016; // In units of reactivity
    public static double decayProductRate = 0.997; // Based on the half-life of xenon-135, using real-life days as
    // Minecraft days, and yes, I am using this for plutonium too
    public static double poisonFraction = 0.063; // Xenon-135 yield from fission
    public static double crossSectionRatio = 4; // The ratio between the cross section for typical fuels and xenon-135;
    // very much changed here for balance purposes

    public double coolantMass;
    public double fuelMass;
    public double structuralMass;
    public boolean needsOutput;
    public boolean controlRodRegulationOn = true;
    public boolean isOn = false;

    protected static double responseFunction(double target, double current, double criticalRate) {
        if (current <= 0) {
            if (criticalRate < 1) {
                return 0;
            } else {
                current = 0.1;
            }
        }
        double expDecay = Math.exp(-criticalRate);
        return current * expDecay + target * (1 - expDecay);
    }

    protected double responseFunctionTemperature(double envTemperature, double currentTemperature, double heatAdded,
                                                 double heatAbsorbed) {
        currentTemperature = Math.max(0.1, currentTemperature);
        heatAbsorbed = Math.max(0.1, heatAbsorbed);
        /*
         * Simplifies what is the following:
         * heatTransferCoefficient = 1 / (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity);
         * (https://en.wikipedia.org/wiki/Newton%27s_law_of_cooling#First-order_transient_response_of_lumped-
         * capacitance_objects)
         * This assumes that we're extracting heat from the reactor through the wall into slowly moving air, removing
         * the second convective heat.
         * timeConstant = heatTransferCoefficient * this.surfaceArea / specificHeatCapacity;
         */
        double timeConstant = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea;

        // Solves the following differential equation:

        // dT/dt = h_added_tot / m_tot - k(T - T_env) at t = 1s with T(0) = T_0
        double expDecay = Math.exp(-timeConstant);

        double effectiveEnvTemperature = envTemperature +
                (heatAdded - heatAbsorbed) / (timeConstant * (this.coolantMass + this.structuralMass + this.fuelMass));
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay);
    }

    public FissionReactor(int size, int depth, double controlRodInsertion) {
        reactorLayout = new ReactorComponent[size][size];
        reactorDepth = depth;
        reactorRadius = (double) size / 2 + 1.5; // Includes the extra block plus the distance from the center of a
        // block to its edge
        // Maps (0, 15) -> (0.01, 1)
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        fuelRods = new ArrayList<>();
        controlRods = new ArrayList<>();
        coolantChannels = new ArrayList<>();
        effectiveControlRods = new ArrayList<>();
        effectiveCoolantChannels = new ArrayList<>();
        // 2pi * r^2 + 2pi * r * l
        surfaceArea = (reactorRadius * reactorRadius) * Math.PI * 2 + reactorDepth * reactorRadius * Math.PI * 2;
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI *
                300; // Assuming 300 kg/m^3 when it's basically empty, does not have to be precise
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
        moderatorTipped = false;
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
                for (int t = 0; t < ConfigHolder.machines.fissionReactorResolution; t++) {
                    double[] pos = new double[2];
                    pos[0] = (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[0]) *
                            ((float) t / ConfigHolder.machines.fissionReactorResolution) + fuelRods.get(i).getPos()[0];
                    pos[1] = (fuelRods.get(j).getPos()[1] - fuelRods.get(i).getPos()[1]) *
                            ((float) t / ConfigHolder.machines.fissionReactorResolution) + fuelRods.get(i).getPos()[1];
                    ReactorComponent component = reactorLayout[(int) Math.round(pos[0])][(int) Math.round(pos[1])];

                    if (component == null) {
                        continue;
                    }
                    mij += component.getModerationFactor();

                    /*
                     * For simplicity, we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel
                     * rods are ignored as obstructed
                     */
                    if (component instanceof FuelRod && !component.samePositionAs(fuelRods.get(i)) &&
                            !component.samePositionAs(fuelRods.get(j))) {
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
                            if (((ControlRod) component).hasModeratorTip()) {
                                moderatorTipped = true;
                            }
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
                    mij /= ConfigHolder.machines.fissionReactorResolution;
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
            avgHighEnergyFissionFactor += i.getHEFissionFactor();
            avgLowEnergyFissionFactor += i.getLEFissionFactor();
            avgHighEnergyCaptureFactor += i.getHECaptureFactor();
            avgLowEnergyCaptureFactor += i.getLECaptureFactor();

        }

        if (fuelRods.size() > 1) {
            avgGeometricFactorSlowNeutrons *= 0.125 / fuelRods.size();
            avgGeometricFactorFastNeutrons *= 0.125 / fuelRods.size();

            avgHighEnergyFissionFactor /= fuelRods.size();
            avgLowEnergyFissionFactor /= fuelRods.size();
            avgHighEnergyCaptureFactor /= fuelRods.size();
            avgLowEnergyCaptureFactor /= fuelRods.size();

            avgFuelRodDistance /= 2. * fuelRods.size();

            double kSlow = avgLowEnergyFissionFactor / avgLowEnergyCaptureFactor * avgGeometricFactorSlowNeutrons;
            double kFast = avgHighEnergyFissionFactor / avgHighEnergyCaptureFactor * avgGeometricFactorFastNeutrons;

            k = (kSlow + kFast) * reactorDepth / (1. + reactorDepth);
            double depthDiameterDifference = 0.5 * (reactorDepth - reactorRadius * 2) / reactorRadius;
            double sigmoid = 1 / (1 + Math.exp(-depthDiameterDifference));
            double fuelRodFactor = sigmoid * Math.pow(avgFuelRodDistance, -2) +
                    (1 - sigmoid) * Math.pow(avgFuelRodDistance, -1);

            maxPower = fuelRods.size() * (avgHighEnergyFissionFactor + avgLowEnergyFissionFactor) * fuelRodFactor *
                    ConfigHolder.machines.nuclearPowerMultiplier;
        } else {
            // The calculations break down for the geometry, so we just do this instead.
            k = 0.00001;
            maxPower = 0.1 * ConfigHolder.machines.nuclearPowerMultiplier;
        }
        /*
         * We give each control rod and coolant channel a weight depending on how many fuel rods they affect
         */
        this.computeControlRodWeights();
        this.computeCoolantChannelWeights();

        controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);

        this.prepareInitialConditions();
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

    public boolean isDepleted() {
        return maxFuelDepletion <= fuelDepletion || fuelDepletion < 0;
    }

    public void prepareInitialConditions() {
        coolantBaseTemperature = 0;
        coolantBoilingPointStandardPressure = 0;
        avgPressure = 0;
        coolantHeatOfVaporization = 0;
        maxFuelDepletion = 0;
        for (FuelRod rod : fuelRods) {
            maxFuelDepletion += rod.getFuel().getDuration();
        }
        if (fuelDepletion < 0) {
            fuelDepletion = maxFuelDepletion;
        }

        for (CoolantChannel channel : effectiveCoolantChannels) {

            CoolantProperty prop = channel.getCoolant().getProperty(PropertyKey.COOLANT);

            coolantBaseTemperature += channel.getCoolant().getFluid().getTemperature() *
                    channel.getWeight();
            coolantBoilingPointStandardPressure += prop.getBoilingPoint() *
                    channel.getWeight();
            avgPressure += prop.getPressure() *
                    channel.getWeight();
            coolantHeatOfVaporization += prop.getHeatOfVaporization() *
                    channel.getWeight();
        }

        if (coolantBaseTemperature == 0) {
            coolantBaseTemperature = envTemperature;
        }
        if (avgPressure == 0) {
            avgPressure = standardPressure;
        }
        if (coolantBoilingPointStandardPressure == 0) {
            coolantBoilingPointStandardPressure = airBoilingPoint;
        }
        isOn = true;
    }

    /**
     * Consumes the coolant. Calculates the heat removed by the coolant based on an amalgamation of different equations.
     * It is not particularly realistic, but allows for some fine-tuning to happen. Heat removed is proportional to the
     * surface area of the coolant channel (which is equivalent to the reactor's depth), as well as the flow rate of
     * coolant and the difference in temperature between the reactor and the coolant
     */
    public void makeCoolantFlow(int flowRate) {
        for (CoolantChannel channel : coolantChannels) {
            FluidStack tryFluidDrain = channel.getInputHandler().getFluidTank().drain(flowRate, false);
            if (tryFluidDrain != null) {
                int drained = tryFluidDrain.amount;

                Material coolant = channel.getCoolant();

                CoolantProperty prop = coolant.getProperty(PropertyKey.COOLANT);

                double heatRemovedPerLiter = prop.getSpecificHeatCapacity() *
                        (prop.getHotHPCoolant().getFluid().getTemperature() - coolant.getFluid().getTemperature()) /
                        prop.getSpecialCoolantAbsorption();
                // Explained by:
                // https://physics.stackexchange.com/questions/153434/heat-transfer-between-the-bulk-of-the-fluid-inside-the-pipe-and-the-pipe-externa
                double heatFluxPerAreaAndTemp = 1 /
                        (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity);
                double idealHeatFlux = heatFluxPerAreaAndTemp * 4 * reactorDepth *
                        (temperature - coolant.getFluid().getTemperature());

                double idealFluidUsed = idealHeatFlux / heatRemovedPerLiter;

                int remainingSpace = channel.getOutputHandler().getFluidTank().getCapacity() -
                        channel.getOutputHandler().getFluidTank().getFluidAmount();
                int actualFlowRate = Math.min(Math.min(remainingSpace, drained),
                        (int) (idealFluidUsed + channel.partialCoolant));
                // Should occasionally decrease when coolant is actually consumed.
                channel.partialCoolant += Math.max(0, Math.min(idealFluidUsed, drained)) - actualFlowRate;

                FluidStack HPCoolant = new FluidStack(
                        prop.getHotHPCoolant().getFluid(), actualFlowRate);

                channel.getInputHandler().getFluidTank().drain(actualFlowRate, true);

                channel.getOutputHandler().getFluidTank().fill(HPCoolant, true);

                if (prop.accumulatesHydrogen() &&
                        this.temperature > zircaloyHydrogenReactionTemperature) {
                    double boilingPoint = coolantBoilingPoint(coolant);
                    if (this.temperature > boilingPoint) {
                        this.accumulatedHydrogen += (this.temperature - boilingPoint) / boilingPoint;
                    } else if (actualFlowRate < Math.min(remainingSpace, idealFluidUsed)) {
                        this.accumulatedHydrogen += (this.temperature - zircaloyHydrogenReactionTemperature) /
                                zircaloyHydrogenReactionTemperature;
                    }
                }

                this.coolantMass += actualFlowRate * coolant.getMass();
                this.heatRemoved += actualFlowRate * heatRemovedPerLiter;
            }
        }
        this.coolantMass /= 1000;
        this.accumulatedHydrogen *= 0.98;
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
                R * Math.log(this.pressure / standardPressure) /
                        coolant.getProperty(PropertyKey.COOLANT).getHeatOfVaporization());
    }

    public void updateTemperature() {
        this.temperature = responseFunctionTemperature(envTemperature, this.temperature, this.power * 1000000,
                this.heatRemoved);
        this.heatRemoved = 0;
    }

    public void updatePressure() {
        this.pressure = responseFunction(
                this.temperature <= this.coolantBoilingPoint() || !this.isOn ? this.exteriorPressure :
                        1000. * R * this.temperature,
                this.pressure, 0.2);
    }

    public void updateNeutronPoisoning() {
        this.neutronPoisonAmount += this.decayProductsAmount * (1 - decayProductRate) * poisonFraction;
        this.neutronPoisonAmount *= decayProductRate;
        this.neutronPoisonAmount -= this.neutronPoisonAmount * crossSectionRatio * power / surfaceArea;
    }

    public double getDecayHeat() {
        return this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1;
    }

    /*
     * public double voidContribution() {
     * return this.temperature > this.coolantBoilingPoint() ? this.voidFactor() * this.maxPressure : 0.;
     * }
     */

    public void updatePower() {
        if (this.isOn) {
            this.kEff = this.k + controlRodFactor;
            // Since the power defect is a change in the reactivity rho (1 - 1 / kEff), we have to do this thing.
            // (1 - 1 / k) = rho(k) => rho^-1(rho) = 1 / (1 - rho)
            // rho^-1(rho(k) - defect) is thus 1 / (1 - (1 - 1/k - defect)) = 1 / (1/k + defect)
            this.kEff = 1 / ((1 / this.kEff) + powerDefectCoefficient * (this.power / this.maxPower) +
                    neutronPoisonAmount * crossSectionRatio / surfaceArea);
            this.kEff = Math.max(0, this.kEff);
            this.prevFuelDepletion = this.fuelDepletion;
            // maps (1, 1.1) to (1, 15); this value basically sets how quickly kEff operates
            this.criticalRodInsertion = (int) Math.max(1, Math.min(15, (kEff - 1) * 150.));

            this.power = responseFunction(Math.min(this.realMaxPower(), this.power * kEff + 0.0001), this.power,
                    this.criticalRodInsertion);
            this.fuelDepletion += this.power * 1000;

            this.decayProductsAmount += Math.max(this.fuelDepletion - this.prevFuelDepletion, 0.) / 1000;
        } else {
            this.power = responseFunction(Math.min(this.realMaxPower(), this.power * kEff), this.power,
                    1);
        }
        this.decayProductsAmount *= decayProductRate;
    }

    public double realMaxPower() {
        if (this.moderatorTipped && (this.controlRodInsertion <= 9. / 16 && this.controlRodInsertion >= 7. / 16)) {
            return this.maxPower * 1.1;
        } else if (this.controlRodInsertion > this.criticalRodInsertion || this.isDepleted() || !this.isOn) {
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

    public void addComponent(ReactorComponent component, int x, int y) {
        reactorLayout[x][y] = component;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setDouble("Temperature", this.temperature);
        tagCompound.setDouble("Pressure", this.pressure);
        tagCompound.setDouble("Power", this.power);
        tagCompound.setDouble("FuelDepletion", this.fuelDepletion);
        tagCompound.setDouble("AccumulatedHydrogen", this.accumulatedHydrogen);
        tagCompound.setDouble("HeatRemoved", this.heatRemoved);
        tagCompound.setDouble("NeutronPoisonAmount", this.neutronPoisonAmount);
        tagCompound.setDouble("DecayProductsAmount", this.decayProductsAmount);
        tagCompound.setBoolean("NeedsOutput", this.needsOutput);
        tagCompound.setDouble("ControlRodInsertion", this.controlRodInsertion);
        tagCompound.setBoolean("IsOn", this.isOn);
        tagCompound.setBoolean("ControlRodRegulationOn", this.controlRodRegulationOn);

        return tagCompound;
    }

    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.temperature = tagCompound.getDouble("Temperature");
        this.pressure = tagCompound.getDouble("Pressure");
        this.power = tagCompound.getDouble("Power");
        this.fuelDepletion = tagCompound.getDouble("FuelDepletion");
        this.accumulatedHydrogen = tagCompound.getDouble("AccumulatedHydrogen");
        this.heatRemoved = tagCompound.getDouble("HeatRemoved");
        this.neutronPoisonAmount = tagCompound.getDouble("NeutronPoisonAmount");
        this.decayProductsAmount = tagCompound.getDouble("DecayProductsAmount");
        this.needsOutput = tagCompound.getBoolean("NeedsOutput");
        this.controlRodInsertion = tagCompound.getDouble("ControlRodInsertion");
        this.isOn = tagCompound.getBoolean("IsOn");
        this.controlRodRegulationOn = tagCompound.getBoolean("ControlRodRegulationOn");
    }

    public void updateControlRodInsertion(double controlRodInsertion) {
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
    }

    public void regulateControlRods() {
        if (!this.isOn || !this.controlRodRegulationOn)
            return;
        double load = Math.max(temperature / maxTemperature, pressure / maxPressure);
        if (load > 1. / 40 && kEff > 1.02) {
            this.controlRodInsertion += 5f / 255;
            this.controlRodInsertion = Math.min(1, this.controlRodInsertion);
            this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
        }
        if (load < 9. / 10) {
            if (kEff < 1) {
                this.controlRodInsertion -= 1f / 255;
                this.controlRodInsertion = Math.max(0, this.controlRodInsertion);
                this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
            }
        } else {
            if (kEff > 1) {
                this.controlRodInsertion += 1f / 255;
                this.controlRodInsertion = Math.min(1, this.controlRodInsertion);
                this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
            }
        }
    }

    public void turnOff() {
        this.isOn = false;
        this.maxPower = 0;
        this.k = 0;
        this.kEff = 0;
        this.coolantMass = 0;
        this.fuelMass = 0;
        reactorLayout = new ReactorComponent[reactorLayout.length][reactorLayout.length];
        fuelRods.clear();
        controlRods.clear();
        coolantChannels.clear();
        effectiveControlRods.clear();
        effectiveCoolantChannels.clear();
    }
}
